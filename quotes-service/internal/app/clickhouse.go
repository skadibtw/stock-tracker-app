package app

import (
	"bufio"
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"sync"
	"time"

	"stock-tracker-app/quotes-service/internal/quotes"
)

type clickHouseRow struct {
	Ticker      string  `json:"ticker"`
	Price       float64 `json:"price"`
	CollectedAt string  `json:"collected_at"`
	Source      string  `json:"source"`
}

type ClickHouseHistoryStore struct {
	endpoint string
	database string
	username string
	password string
	client   *http.Client

	mu        sync.RWMutex
	healthy   bool
	lastError string
}

func NewClickHouseHistoryStore(ctx context.Context, cfg Config) (*ClickHouseHistoryStore, error) {
	store := &ClickHouseHistoryStore{
		endpoint: strings.TrimRight(cfg.ClickHouseEndpoint, "/"),
		database: cfg.ClickHouseDatabase,
		username: cfg.ClickHouseUsername,
		password: cfg.ClickHousePassword,
		client: &http.Client{
			Timeout: 10 * time.Second,
		},
		healthy: true,
	}

	if err := store.exec(ctx, fmt.Sprintf("CREATE DATABASE IF NOT EXISTS %s", store.database), nil); err != nil {
		return nil, err
	}
	if err := store.execOnDatabase(ctx, store.database, `CREATE TABLE IF NOT EXISTS quotes_history (
			ticker String,
			price Float64,
			collected_at String,
			source String
		) ENGINE = MergeTree
		ORDER BY (ticker, collected_at)`, nil); err != nil {
		return nil, err
	}

	return store, nil
}

func (s *ClickHouseHistoryStore) Store(ctx context.Context, items []quotes.Quote) error {
	if len(items) == 0 {
		return nil
	}

	var body bytes.Buffer
	encoder := json.NewEncoder(&body)
	for _, item := range items {
		if err := encoder.Encode(clickHouseRow{
			Ticker:      item.Ticker,
			Price:       item.Price,
			CollectedAt: item.Timestamp,
			Source:      item.Source,
		}); err != nil {
			s.markFailure(err.Error())
			return err
		}
	}

	query := "INSERT INTO quotes_history (ticker, price, collected_at, source) FORMAT JSONEachRow"
	if err := s.execOnDatabase(ctx, s.database, query, &body); err != nil {
		s.markFailure(err.Error())
		return err
	}

	s.markHealthy()
	return nil
}

func (s *ClickHouseHistoryStore) History(ctx context.Context, ticker string, limit int) ([]quotes.Quote, error) {
	if limit <= 0 {
		limit = 50
	}

	ticker = sanitizeTicker(ticker)
	query := fmt.Sprintf(
		"SELECT ticker, price, collected_at, source FROM quotes_history WHERE ticker = '%s' ORDER BY collected_at DESC LIMIT %d FORMAT JSONEachRow",
		ticker,
		limit,
	)
	responseBody, err := s.query(ctx, query)
	if err != nil {
		s.markFailure(err.Error())
		return nil, err
	}
	defer responseBody.Close()

	items := make([]quotes.Quote, 0, limit)
	scanner := bufio.NewScanner(responseBody)
	for scanner.Scan() {
		var row clickHouseRow
		if err := json.Unmarshal(scanner.Bytes(), &row); err != nil {
			return nil, err
		}
		items = append(items, quotes.Quote{
			Ticker:    row.Ticker,
			Price:     row.Price,
			Timestamp: row.CollectedAt,
			Source:    row.Source,
		})
	}
	if err := scanner.Err(); err != nil {
		return nil, err
	}

	s.markHealthy()
	return items, nil
}

func (s *ClickHouseHistoryStore) DependencyStatus() quotes.DependencyStatus {
	s.mu.RLock()
	defer s.mu.RUnlock()

	status := "up"
	if !s.healthy {
		status = "down"
	}

	return quotes.DependencyStatus{
		Name:      "clickhouse",
		Status:    status,
		LastError: s.lastError,
	}
}

func (s *ClickHouseHistoryStore) exec(ctx context.Context, query string, body io.Reader) error {
	responseBody, err := s.do(ctx, "default", query, body)
	if responseBody != nil {
		defer responseBody.Close()
	}
	return err
}

func (s *ClickHouseHistoryStore) execOnDatabase(ctx context.Context, database string, query string, body io.Reader) error {
	responseBody, err := s.do(ctx, database, query, body)
	if responseBody != nil {
		defer responseBody.Close()
	}
	return err
}

func (s *ClickHouseHistoryStore) query(ctx context.Context, query string) (io.ReadCloser, error) {
	return s.do(ctx, s.database, query, nil)
}

func (s *ClickHouseHistoryStore) do(ctx context.Context, database string, query string, body io.Reader) (io.ReadCloser, error) {
	requestURL := fmt.Sprintf("%s/?database=%s&query=%s", s.endpoint, url.QueryEscape(database), url.QueryEscape(query))
	request, err := http.NewRequestWithContext(ctx, http.MethodPost, requestURL, body)
	if err != nil {
		return nil, err
	}
	request.SetBasicAuth(s.username, s.password)

	response, err := s.client.Do(request)
	if err != nil {
		return nil, err
	}
	if response.StatusCode >= 300 {
		defer response.Body.Close()
		payload, _ := io.ReadAll(response.Body)
		return nil, fmt.Errorf("clickhouse returned %s: %s", response.Status, strings.TrimSpace(string(payload)))
	}

	return response.Body, nil
}

func (s *ClickHouseHistoryStore) markHealthy() {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.healthy = true
	s.lastError = ""
}

func (s *ClickHouseHistoryStore) markFailure(message string) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.healthy = false
	s.lastError = message
}

func sanitizeTicker(value string) string {
	value = strings.ToUpper(strings.TrimSpace(value))
	var builder strings.Builder
	for _, symbol := range value {
		if (symbol >= 'A' && symbol <= 'Z') || (symbol >= '0' && symbol <= '9') || symbol == '_' || symbol == '-' {
			builder.WriteRune(symbol)
		}
	}
	return builder.String()
}
