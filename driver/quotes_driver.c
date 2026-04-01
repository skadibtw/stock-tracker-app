#include <linux/cdev.h>
#include <linux/device.h>
#include <linux/fs.h>
#include <linux/init.h>
#include <linux/kernel.h>
#include <linux/module.h>
#include <linux/proc_fs.h>
#include <linux/random.h>
#include <linux/rtc.h>
#include <linux/seq_file.h>
#include <linux/spinlock.h>
#include <linux/timekeeping.h>
#include <linux/timer.h>
#include <linux/uaccess.h>
#include <linux/version.h>

#define DEVICE_NAME "quotes"
#define PROC_STATS_NAME "quotes_stats"
#define QUOTES_COUNT 7
#define DEFAULT_UPDATE_INTERVAL_MS 500
#define DEFAULT_MAX_DELTA_CENTS 50
#define SNAPSHOT_BUFFER_SIZE 512
#define MIN_PRICE_CENTS 100

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Codex");
MODULE_DESCRIPTION("MVP Linux quotes simulator driver");

static unsigned int update_interval_ms = DEFAULT_UPDATE_INTERVAL_MS;
module_param(update_interval_ms, uint, 0644);
MODULE_PARM_DESC(update_interval_ms, "Quotes update interval in milliseconds");

static unsigned int max_delta_cents = DEFAULT_MAX_DELTA_CENTS;
module_param(max_delta_cents, uint, 0644);
MODULE_PARM_DESC(max_delta_cents, "Maximum random quote change per update in cents");

struct simulated_quote {
	const char *ticker;
	long price_cents;
	time64_t updated_at;
};

static struct simulated_quote quotes[QUOTES_COUNT] = {
	{ "AAPL", 21455, 0 },
	{ "TSLA", 17820, 0 },
	{ "SBER", 30110, 0 },
	{ "GAZP", 17235, 0 },
	{ "YNDX", 41250, 0 },
	{ "VTBR", 985, 0 },
	{ "LKOH", 73410, 0 },
};

static dev_t quotes_dev;
static struct cdev quotes_cdev;
static struct class *quotes_class;
static struct device *quotes_device;
static struct timer_list quotes_timer;
static spinlock_t quotes_lock;
static struct proc_dir_entry *quotes_stats_entry;
static atomic64_t read_count = ATOMIC64_INIT(0);
static atomic64_t update_count = ATOMIC64_INIT(0);
static time64_t last_update_at;

/* format_timestamp renders kernel time into the RFC3339 layout used by the service. */
static void format_timestamp(time64_t seconds, char *buffer, size_t size)
{
	struct rtc_time tm;

	rtc_time64_to_tm(seconds, &tm);
	snprintf(buffer, size, "%04d-%02d-%02dT%02d:%02d:%02dZ",
		 tm.tm_year + 1900,
		 tm.tm_mon + 1,
		 tm.tm_mday,
		 tm.tm_hour,
		 tm.tm_min,
		 tm.tm_sec);
}

/* update_quote_prices applies a small random delta and rearms the timer. */
static void update_quote_prices(struct timer_list *unused)
{
	unsigned long flags;
	int i;
	time64_t now = ktime_get_real_seconds();

	spin_lock_irqsave(&quotes_lock, flags);
	for (i = 0; i < QUOTES_COUNT; i++) {
		int delta = 0;
		long next_price;

		if (max_delta_cents > 0)
			delta = (int)get_random_u32_below((max_delta_cents * 2) + 1) - (int)max_delta_cents;
		next_price = quotes[i].price_cents + delta;

		if (next_price < MIN_PRICE_CENTS)
			next_price = MIN_PRICE_CENTS;

		quotes[i].price_cents = next_price;
		quotes[i].updated_at = now;
	}
	last_update_at = now;
	spin_unlock_irqrestore(&quotes_lock, flags);

	atomic64_inc(&update_count);
	mod_timer(&quotes_timer, jiffies + msecs_to_jiffies(update_interval_ms));
}

/* quotes_read returns the current quotes snapshot for each open/read cycle. */
static ssize_t quotes_read(struct file *file, char __user *buf, size_t count, loff_t *ppos)
{
	unsigned long flags;
	char snapshot[SNAPSHOT_BUFFER_SIZE];
	size_t snapshot_len = 0;
	int i;

	if (*ppos < 0)
		return -EINVAL;
	if (*ppos == 0)
		atomic64_inc(&read_count);

	spin_lock_irqsave(&quotes_lock, flags);
	for (i = 0; i < QUOTES_COUNT; i++) {
		char timestamp[32];
		long price_int = quotes[i].price_cents / 100;
		long price_frac = quotes[i].price_cents % 100;

		format_timestamp(quotes[i].updated_at, timestamp, sizeof(timestamp));
		snapshot_len += scnprintf(snapshot + snapshot_len,
					  sizeof(snapshot) - snapshot_len,
					  "%s %ld.%02ld %s\n",
					  quotes[i].ticker,
					  price_int,
					  price_frac,
					  timestamp);
		if (snapshot_len >= sizeof(snapshot))
			break;
	}
	spin_unlock_irqrestore(&quotes_lock, flags);

	if (*ppos >= snapshot_len)
		return 0;

	if (count > snapshot_len - *ppos)
		count = snapshot_len - *ppos;

	if (copy_to_user(buf, snapshot + *ppos, count))
		return -EFAULT;

	*ppos += count;
	return count;
}

static int quotes_stats_show(struct seq_file *m, void *v)
{
	unsigned long flags;
	char timestamp[32];
	time64_t current_last_update;

	spin_lock_irqsave(&quotes_lock, flags);
	current_last_update = last_update_at;
	spin_unlock_irqrestore(&quotes_lock, flags);

	format_timestamp(current_last_update, timestamp, sizeof(timestamp));
	seq_printf(m, "device=%s\n", DEVICE_NAME);
	seq_printf(m, "tickers=%d\n", QUOTES_COUNT);
	seq_printf(m, "update_interval_ms=%u\n", update_interval_ms);
	seq_printf(m, "max_delta_cents=%u\n", max_delta_cents);
	seq_printf(m, "read_count=%lld\n", atomic64_read(&read_count));
	seq_printf(m, "update_count=%lld\n", atomic64_read(&update_count));
	seq_printf(m, "last_update=%s\n", timestamp);

	return 0;
}

static int quotes_stats_open(struct inode *inode, struct file *file)
{
	return single_open(file, quotes_stats_show, NULL);
}

#if LINUX_VERSION_CODE >= KERNEL_VERSION(5, 6, 0)
static const struct proc_ops quotes_stats_proc_ops = {
	.proc_open = quotes_stats_open,
	.proc_read = seq_read,
	.proc_lseek = seq_lseek,
	.proc_release = single_release,
};
#else
static const struct file_operations quotes_stats_proc_ops = {
	.owner = THIS_MODULE,
	.open = quotes_stats_open,
	.read = seq_read,
	.llseek = seq_lseek,
	.release = single_release,
};
#endif

static const struct file_operations quotes_fops = {
	.owner = THIS_MODULE,
	.read = quotes_read,
};

/* quotes_init registers /dev/quotes and starts periodic price updates. */
static int __init quotes_init(void)
{
	int ret;
	int i;
	time64_t now;

	if (update_interval_ms == 0)
		update_interval_ms = DEFAULT_UPDATE_INTERVAL_MS;

	spin_lock_init(&quotes_lock);
	now = ktime_get_real_seconds();
	last_update_at = now;

	for (i = 0; i < QUOTES_COUNT; i++)
		quotes[i].updated_at = now;

	ret = alloc_chrdev_region(&quotes_dev, 0, 1, DEVICE_NAME);
	if (ret)
		return ret;

	cdev_init(&quotes_cdev, &quotes_fops);
	quotes_cdev.owner = THIS_MODULE;

	ret = cdev_add(&quotes_cdev, quotes_dev, 1);
	if (ret)
		goto unregister_region;

#if LINUX_VERSION_CODE >= KERNEL_VERSION(6, 4, 0)
	quotes_class = class_create(DEVICE_NAME);
#else
	quotes_class = class_create(THIS_MODULE, DEVICE_NAME);
#endif
	if (IS_ERR(quotes_class)) {
		ret = PTR_ERR(quotes_class);
		goto remove_cdev;
	}

	quotes_device = device_create(quotes_class, NULL, quotes_dev, NULL, DEVICE_NAME);
	if (IS_ERR(quotes_device)) {
		ret = PTR_ERR(quotes_device);
		goto destroy_class;
	}

	quotes_stats_entry = proc_create(PROC_STATS_NAME, 0444, NULL, &quotes_stats_proc_ops);
	if (!quotes_stats_entry) {
		ret = -ENOMEM;
		goto destroy_device;
	}

	timer_setup(&quotes_timer, update_quote_prices, 0);
	mod_timer(&quotes_timer, jiffies + msecs_to_jiffies(update_interval_ms));

	pr_info("quotes_driver loaded: /dev/%s, /proc/%s\n", DEVICE_NAME, PROC_STATS_NAME);
	return 0;

destroy_device:
	device_destroy(quotes_class, quotes_dev);
destroy_class:
	class_destroy(quotes_class);
remove_cdev:
	cdev_del(&quotes_cdev);
unregister_region:
	unregister_chrdev_region(quotes_dev, 1);
	return ret;
}

/* quotes_exit stops updates and removes the character device. */
static void __exit quotes_exit(void)
{
	del_timer_sync(&quotes_timer);
	if (quotes_stats_entry)
		proc_remove(quotes_stats_entry);
	device_destroy(quotes_class, quotes_dev);
	class_destroy(quotes_class);
	cdev_del(&quotes_cdev);
	unregister_chrdev_region(quotes_dev, 1);
	pr_info("quotes_driver unloaded\n");
}

module_init(quotes_init);
module_exit(quotes_exit);
