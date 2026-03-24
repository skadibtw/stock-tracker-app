package quotes

import "testing"

func TestStoreHistorySkipsDuplicateSnapshots(t *testing.T) {
	t.Parallel()

	store := NewStoreWithHistoryLimit(5)
	item := Quote{Ticker: "GAZP", Price: 172.35, Timestamp: "2026-03-24T15:10:00Z", Source: "linux-driver"}

	store.SetQuotes([]Quote{item})
	store.SetQuotes([]Quote{item})

	history, found, available := store.GetHistory("GAZP", 0)
	if !available || !found {
		t.Fatalf("history should be available, found=%v available=%v", found, available)
	}
	if len(history) != 1 {
		t.Fatalf("expected one history item, got %d", len(history))
	}
}

func TestStoreHistoryRespectsLimit(t *testing.T) {
	t.Parallel()

	store := NewStoreWithHistoryLimit(2)
	store.SetQuotes([]Quote{{Ticker: "GAZP", Price: 172.35, Timestamp: "2026-03-24T15:10:00Z", Source: "linux-driver"}})
	store.SetQuotes([]Quote{{Ticker: "GAZP", Price: 173.35, Timestamp: "2026-03-24T15:10:01Z", Source: "linux-driver"}})
	store.SetQuotes([]Quote{{Ticker: "GAZP", Price: 174.35, Timestamp: "2026-03-24T15:10:02Z", Source: "linux-driver"}})

	history, found, available := store.GetHistory("GAZP", 0)
	if !available || !found {
		t.Fatalf("history should be available, found=%v available=%v", found, available)
	}
	if len(history) != 2 {
		t.Fatalf("expected two history items, got %d", len(history))
	}
	if history[0].Price != 173.35 || history[1].Price != 174.35 {
		t.Fatalf("unexpected history payload: %+v", history)
	}
}
