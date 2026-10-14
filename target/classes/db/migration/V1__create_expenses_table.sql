CREATE TABLE expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    category VARCHAR(30) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_date ON expenses (expense_date);
CREATE INDEX idx_expenses_category ON expenses (category);
