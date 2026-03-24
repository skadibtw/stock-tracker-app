package app

import (
	"encoding/json"
	"net/http"
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

	mux.HandleFunc("/quotes", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			writeJSON(w, http.StatusMethodNotAllowed, map[string]string{"error": "method not allowed"})
			return
		}

		items, ok := store.GetAll()
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

		ticker := strings.TrimSpace(strings.TrimPrefix(r.URL.Path, "/quotes/"))
		if ticker == "" {
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
