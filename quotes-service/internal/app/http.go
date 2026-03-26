package app

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"strings"

	"stock-tracker-app/quotes-service/internal/quotes"
)

// NewHTTPHandler exposes the MVP read-only quotes API.
func NewHTTPHandler(store *quotes.Store) http.Handler {
	mux := http.NewServeMux()

	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
			return
		}

		writeJSON(w, http.StatusOK, store.Health())
	})

	mux.HandleFunc("/metrics", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
			return
		}

		writeMetrics(w, store.Metrics())
	})

	mux.HandleFunc("/quotes", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
			return
		}

		items, ok := store.GetFiltered(parseTickersQuery(r.URL.Query().Get("tickers")))
		if !ok {
			writeJSON(w, http.StatusServiceUnavailable, map[string]string{"error": "quotes source unavailable"})
			return
		}

		writeJSON(w, http.StatusOK, items)
	})

	mux.HandleFunc("/quotes/", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
			return
		}

		path := strings.TrimSpace(strings.TrimPrefix(r.URL.Path, "/quotes/"))
		if path == "" {
			http.NotFound(w, r)
			return
		}

		if strings.HasSuffix(path, "/history") {
			ticker := strings.TrimSuffix(path, "/history")
			ticker = strings.Trim(ticker, "/ ")
			if ticker == "" || strings.Contains(ticker, "/") {
				http.NotFound(w, r)
				return
			}

			limit, err := parseOptionalLimit(r.URL.Query().Get("limit"))
			if err != nil {
				writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid history limit"})
				return
			}

			items, found, available := store.GetHistory(ticker, limit)
			if !available {
				writeJSON(w, http.StatusServiceUnavailable, map[string]string{"error": "quotes source unavailable"})
				return
			}
			if !found {
				writeJSON(w, http.StatusNotFound, map[string]string{"error": "ticker not found"})
				return
			}

			writeJSON(w, http.StatusOK, items)
			return
		}

		ticker := strings.Trim(path, "/ ")
		if ticker == "" || strings.Contains(ticker, "/") {
			http.NotFound(w, r)
			return
		}

		item, found, available := store.GetByTicker(ticker)
		if !available {
			writeJSON(w, http.StatusServiceUnavailable, map[string]string{"error": "quotes source unavailable"})
			return
		}
		if !found {
			writeJSON(w, http.StatusNotFound, map[string]string{"error": "ticker not found"})
			return
		}

		writeJSON(w, http.StatusOK, item)
	})

	return mux
}

// writeJSON sends a JSON response with the requested status code.
func writeJSON(w http.ResponseWriter, status int, payload any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(payload)
}

func writeMetrics(w http.ResponseWriter, metrics quotes.Metrics) {
	sourceAvailable := 0
	if metrics.SourceAvailable {
		sourceAvailable = 1
	}

	w.Header().Set("Content-Type", "text/plain; version=0.0.4")
	w.WriteHeader(http.StatusOK)
	_, _ = fmt.Fprintf(
		w,
		"quotes_source_available %d\nquotes_tracked_tickers %d\nquotes_total_refreshes %d\nquotes_total_refresh_errors %d\nquotes_total_parse_errors %d\n",
		sourceAvailable,
		metrics.TrackedTickers,
		metrics.TotalRefreshes,
		metrics.TotalRefreshErrors,
		metrics.TotalParseErrors,
	)
}

func parseTickersQuery(raw string) []string {
	if raw == "" {
		return nil
	}

	parts := strings.Split(raw, ",")
	tickers := make([]string, 0, len(parts))
	for _, part := range parts {
		ticker := strings.TrimSpace(part)
		if ticker == "" {
			continue
		}
		tickers = append(tickers, ticker)
	}

	return tickers
}

func parseOptionalLimit(raw string) (int, error) {
	if raw == "" {
		return 0, nil
	}

	limit, err := strconv.Atoi(raw)
	if err != nil || limit <= 0 {
		return 0, fmt.Errorf("invalid limit")
	}

	return limit, nil
}
