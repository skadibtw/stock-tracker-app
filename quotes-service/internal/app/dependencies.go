package app

import (
	"context"

	"stock-tracker-app/quotes-service/internal/quotes"
)

type QuoteSink interface {
	Store(context.Context, []quotes.Quote) error
	DependencyStatus() quotes.DependencyStatus
}

type HistoryProvider interface {
	History(context.Context, string, int) ([]quotes.Quote, error)
	DependencyStatus() quotes.DependencyStatus
}
