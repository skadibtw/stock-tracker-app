package quotes

import (
	"sort"
	"strings"
	"sync"
	"time"
)

type Store struct {
	mu               sync.RWMutex
	items            map[string]Quote
	history          map[string][]Quote
	historyLimit     int
	sourceAvailable  bool
	lastError        string
	lastUpdate       time.Time
	totalRefreshes   uint64
	totalErrors      uint64
	totalParseErrors uint64
}

// NewStore creates an empty in-memory quotes store.
func NewStore() *Store {
	return NewStoreWithHistoryLimit(10)
}

// NewStoreWithHistoryLimit creates a store with bounded per-ticker history.
func NewStoreWithHistoryLimit(historyLimit int) *Store {
	if historyLimit <= 0 {
		historyLimit = 10
	}

	return &Store{
		items:        make(map[string]Quote),
		history:      make(map[string][]Quote),
		historyLimit: historyLimit,
	}
}

// SetQuotes replaces the current snapshot with the latest valid data.
func (s *Store) SetQuotes(items []Quote) {
	next := make(map[string]Quote, len(items))

	s.mu.Lock()
	defer s.mu.Unlock()

	for _, item := range items {
		key := strings.ToUpper(item.Ticker)
		next[key] = item
		s.appendHistoryLocked(key, item)
	}

	s.items = next
	s.sourceAvailable = true
	s.lastError = ""
	s.lastUpdate = time.Now().UTC()
	s.totalRefreshes++
}

// RecordParseErrors accumulates malformed lines that were skipped during polling.
func (s *Store) RecordParseErrors(count int) {
	if count <= 0 {
		return
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	s.totalParseErrors += uint64(count)
}

// MarkUnavailable records that the source could not be read or parsed.
func (s *Store) MarkUnavailable(reason string) {
	s.mu.Lock()
	defer s.mu.Unlock()

	s.sourceAvailable = false
	s.lastError = reason
	s.totalErrors++
}

// GetAll returns a sorted snapshot of all quotes when the source is healthy.
func (s *Store) GetAll() ([]Quote, bool) {
	return s.GetFiltered(nil)
}

// GetFiltered returns all quotes or only the requested tickers.
func (s *Store) GetFiltered(tickers []string) ([]Quote, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if !s.sourceAvailable {
		return nil, false
	}

	items := make([]Quote, 0, len(s.items))
	if len(tickers) == 0 {
		for _, item := range s.items {
			items = append(items, item)
		}
	} else {
		seen := make(map[string]struct{}, len(tickers))
		for _, ticker := range tickers {
			key := strings.ToUpper(strings.TrimSpace(ticker))
			if key == "" {
				continue
			}
			if _, ok := seen[key]; ok {
				continue
			}
			seen[key] = struct{}{}

			item, ok := s.items[key]
			if ok {
				items = append(items, item)
			}
		}
	}

	sort.Slice(items, func(i, j int) bool {
		return items[i].Ticker < items[j].Ticker
	})

	return items, true
}

// GetByTicker returns one quote and reports both availability and existence.
func (s *Store) GetByTicker(ticker string) (Quote, bool, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if !s.sourceAvailable {
		return Quote{}, false, false
	}

	item, ok := s.items[strings.ToUpper(ticker)]
	return item, ok, true
}

// GetHistory returns the latest recorded states for one ticker.
func (s *Store) GetHistory(ticker string, limit int) ([]Quote, bool, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if !s.sourceAvailable {
		return nil, false, false
	}

	history, ok := s.history[strings.ToUpper(ticker)]
	if !ok {
		return nil, false, true
	}

	if limit > 0 && len(history) > limit {
		history = history[len(history)-limit:]
	}

	result := make([]Quote, len(history))
	copy(result, history)
	return result, true, true
}

// Health returns the current service view of the quotes source state.
func (s *Store) Health() HealthStatus {
	s.mu.RLock()
	defer s.mu.RUnlock()

	status := HealthStatus{
		Status:           "ok",
		ServiceStatus:    "ok",
		SourceStatus:     "unavailable",
		TrackedTickers:   len(s.items),
		TotalRefreshes:   s.totalRefreshes,
		TotalParseErrors: s.totalParseErrors,
	}

	if s.sourceAvailable {
		status.SourceStatus = "available"
	}
	if !s.lastUpdate.IsZero() {
		status.LastSuccessfulSync = s.lastUpdate.Format(time.RFC3339)
	}
	if s.lastError != "" {
		status.LastError = s.lastError
	}

	return status
}

// Metrics returns counters useful for debugging and monitoring.
func (s *Store) Metrics() Metrics {
	s.mu.RLock()
	defer s.mu.RUnlock()

	metrics := Metrics{
		SourceAvailable:    s.sourceAvailable,
		TrackedTickers:     len(s.items),
		TotalRefreshes:     s.totalRefreshes,
		TotalRefreshErrors: s.totalErrors,
		TotalParseErrors:   s.totalParseErrors,
	}
	if !s.lastUpdate.IsZero() {
		metrics.LastSuccessfulSync = s.lastUpdate.Format(time.RFC3339)
	}

	return metrics
}

func (s *Store) appendHistoryLocked(ticker string, item Quote) {
	history := s.history[ticker]
	if len(history) > 0 {
		last := history[len(history)-1]
		if last.Price == item.Price && last.Timestamp == item.Timestamp && last.Source == item.Source {
			return
		}
	}

	history = append(history, item)
	if len(history) > s.historyLimit {
		history = history[len(history)-s.historyLimit:]
	}
	s.history[ticker] = history
}
