package app

import (
	"context"
	"errors"
	"io"
	"log"
	"os"
	"strings"
	"time"

	"stock-tracker-app/quotes-service/internal/quotes"
)

type SnapshotReader interface {
	ReadSnapshot(context.Context) ([]byte, error)
}

type FileSnapshotReader struct {
	Path string
}

// ReadSnapshot loads the current quotes snapshot from a file-like source.
func (r FileSnapshotReader) ReadSnapshot(_ context.Context) ([]byte, error) {
	return os.ReadFile(r.Path)
}

type Collector struct {
	reader       SnapshotReader
	store        *quotes.Store
	logger       *log.Logger
	sourceName   string
	pollInterval time.Duration
}

// NewCollector wires the snapshot source to the in-memory quotes store.
func NewCollector(reader SnapshotReader, store *quotes.Store, logger *log.Logger, sourceName string, pollInterval time.Duration) *Collector {
	return &Collector{
		reader:       reader,
		store:        store,
		logger:       logger,
		sourceName:   sourceName,
		pollInterval: pollInterval,
	}
}

// Run starts polling the source until the context is canceled.
func (c *Collector) Run(ctx context.Context) {
	c.collectOnce(ctx)

	ticker := time.NewTicker(c.pollInterval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			c.collectOnce(ctx)
		}
	}
}

// collectOnce refreshes the store from a single source snapshot.
func (c *Collector) collectOnce(ctx context.Context) {
	snapshot, err := c.reader.ReadSnapshot(ctx)
	if err != nil {
		c.store.MarkUnavailable(err.Error())
		c.logger.Printf("quotes source unavailable: %v", err)
		return
	}

	items, malformed, err := parseSnapshot(string(snapshot), c.sourceName, c.logger)
	c.store.RecordParseErrors(malformed)
	if err != nil {
		c.store.MarkUnavailable(err.Error())
		c.logger.Printf("quotes snapshot rejected: %v", err)
		return
	}

	c.store.SetQuotes(items)
}

// parseSnapshot keeps valid lines and rejects an empty or fully broken snapshot.
func parseSnapshot(snapshot, sourceName string, logger *log.Logger) ([]quotes.Quote, int, error) {
	lines := strings.Split(snapshot, "\n")
	items := make([]quotes.Quote, 0, len(lines))
	malformed := 0

	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line == "" {
			continue
		}

		item, err := quotes.ParseLine(line, sourceName)
		if err != nil {
			malformed++
			logger.Printf("skip malformed line %q: %v", line, err)
			continue
		}
		items = append(items, item)
	}

	if len(items) == 0 {
		if strings.TrimSpace(snapshot) == "" {
			return nil, malformed, io.EOF
		}
		return nil, malformed, errors.New("snapshot contains no valid quotes")
	}

	return items, malformed, nil
}
