package quotes

type Quote struct {
	Ticker    string  `json:"ticker"`
	Price     float64 `json:"price"`
	Timestamp string  `json:"timestamp"`
	Source    string  `json:"source"`
}

type HealthStatus struct {
	Status             string `json:"status"`
	ServiceStatus      string `json:"service_status"`
	SourceStatus       string `json:"source_status"`
	LastSuccessfulSync string `json:"last_successful_sync,omitempty"`
	LastError          string `json:"last_error,omitempty"`
	TrackedTickers     int    `json:"tracked_tickers"`
	TotalRefreshes     uint64 `json:"total_refreshes"`
	TotalParseErrors   uint64 `json:"total_parse_errors"`
}

type Metrics struct {
	SourceAvailable    bool
	TrackedTickers     int
	TotalRefreshes     uint64
	TotalRefreshErrors uint64
	TotalParseErrors   uint64
	LastSuccessfulSync string
}
