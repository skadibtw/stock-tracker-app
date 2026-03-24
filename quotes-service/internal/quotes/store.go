package quotes

import (
	"sort"
	"strings"
	"sync"
	"time"
)

type Store struct {
	mu              sync.RWMutex
	items           map[string]Quote
	sourceAvailable bool
	lastError       string
	lastUpdate      time.Time
}

// NewStore creates an empty in-memory quotes store.
func NewStore() *Store {
	return &Store{
		items: make(map[string]Quote),
	}
}

// SetQuotes replaces the current snapshot with the latest valid data.
func (s *Store) SetQuotes(items []Quote) {
	next := make(map[string]Quote, len(items))
	for _, item := range items {
		next[strings.ToUpper(item.Ticker)] = item
	}

	s.mu.Lock()
	defer s.mu.Unlock()

	s.items = next
	s.sourceAvailable = true
	s.lastError = ""
	s.lastUpdate = time.Now().UTC()
}

// MarkUnavailable records that the source could not be read or parsed.
func (s *Store) MarkUnavailable(reason string) {
	s.mu.Lock()
	defer s.mu.Unlock()

	s.sourceAvailable = false
	s.lastError = reason
}

// GetAll returns a sorted snapshot of all quotes when the source is healthy.
func (s *Store) GetAll() ([]Quote, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	if !s.sourceAvailable {
		return nil, false
	}

	items := make([]Quote, 0, len(s.items))
	for _, item := range s.items {
		items = append(items, item)
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

// Health returns the current service view of the quotes source state.
func (s *Store) Health() HealthStatus {
	s.mu.RLock()
	defer s.mu.RUnlock()

	status := HealthStatus{
		Status: "ok",
		Source: "unavailable",
	}

	if s.sourceAvailable {
		status.Source = "available"
	}
	if !s.lastUpdate.IsZero() {
		status.LastUpdate = s.lastUpdate.Format(time.RFC3339)
	}
	if s.lastError != "" {
		status.LastError = s.lastError
	}

	return status
}
