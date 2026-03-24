package quotes

type Quote struct {
	Ticker    string  `json:"ticker"`
	Price     float64 `json:"price"`
	Timestamp string  `json:"timestamp"`
	Source    string  `json:"source"`
}

type HealthStatus struct {
	Status     string `json:"status"`
	Source     string `json:"source"`
	LastUpdate string `json:"last_update,omitempty"`
	LastError  string `json:"last_error,omitempty"`
}
