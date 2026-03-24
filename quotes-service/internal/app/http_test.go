package app

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"stock-tracker-app/quotes-service/internal/quotes"
)

func TestQuotesEndpointReturnsItems(t *testing.T) {
	t.Parallel()

	store := quotes.NewStore()
	store.SetQuotes([]quotes.Quote{
		{Ticker: "GAZP", Price: 172.35, Timestamp: "2026-03-24T15:10:00Z", Source: "linux-driver"},
	})

	req := httptest.NewRequest(http.MethodGet, "/quotes", nil)
	rec := httptest.NewRecorder()

	NewHTTPHandler(store).ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("unexpected status: %d", rec.Code)
	}

	var items []quotes.Quote
	if err := json.NewDecoder(rec.Body).Decode(&items); err != nil {
		t.Fatalf("failed to decode response: %v", err)
	}
	if len(items) != 1 || items[0].Ticker != "GAZP" {
		t.Fatalf("unexpected payload: %+v", items)
	}
}

func TestQuotesEndpointReturns503WhenSourceIsUnavailable(t *testing.T) {
	t.Parallel()

	store := quotes.NewStore()
	store.MarkUnavailable("missing source")

	req := httptest.NewRequest(http.MethodGet, "/quotes", nil)
	rec := httptest.NewRecorder()

	NewHTTPHandler(store).ServeHTTP(rec, req)

	if rec.Code != http.StatusServiceUnavailable {
		t.Fatalf("unexpected status: %d", rec.Code)
	}
}

func TestQuoteByTickerReturns404(t *testing.T) {
	t.Parallel()

	store := quotes.NewStore()
	store.SetQuotes([]quotes.Quote{
		{Ticker: "GAZP", Price: 172.35, Timestamp: "2026-03-24T15:10:00Z", Source: "linux-driver"},
	})

	req := httptest.NewRequest(http.MethodGet, "/quotes/SBER", nil)
	rec := httptest.NewRecorder()

	NewHTTPHandler(store).ServeHTTP(rec, req)

	if rec.Code != http.StatusNotFound {
		t.Fatalf("unexpected status: %d", rec.Code)
	}
}
