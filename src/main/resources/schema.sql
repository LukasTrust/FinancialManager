-- Create USER table
CREATE TABLE IF NOT EXISTS User (
                                      id SERIAL PRIMARY KEY,
                                      firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
    );

-- Create ACCOUNT table
CREATE TABLE IF NOT EXISTS Account (
                                       id SERIAL PRIMARY KEY,
                                       userId INT NOT NULL,
    amountSearchStrings JSON,
    dateSearchStrings JSON,
    counterPartySearchStrings JSON,
    amountInBankAfterSearchStrings JSON,
    FOREIGN KEY (userId) REFERENCES User(id)
    );

-- Create CHECKING-ACCOUNT table
CREATE TABLE IF NOT EXISTS CheckingAccount (
                                               id SERIAL PRIMARY KEY,
                                               FOREIGN KEY (id) REFERENCES Account(id)
    );

-- Create SAVINGS-ACCOUNT table
CREATE TABLE IF NOT EXISTS SavingsAccount (
                                              id SERIAL PRIMARY KEY,
                                              interestRate DOUBLE PRECISION NOT NULL,
                                              interestRateSearchStrings JSON,
                                              FOREIGN KEY (id) REFERENCES Account(id)
    );

-- Create CONTRACT table
CREATE TABLE IF NOT EXISTS Contract (
                                        id SERIAL PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    amount DOUBLE PRECISION NOT NULL,
    startDate TIMESTAMP NOT NULL,
    endDate TIMESTAMP,
    monthsBetweenPayments INT NOT NULL,
    lastUpdatedAt TIMESTAMP NOT NULL,
    contractSearchStrings JSON
    );

-- Create CONTRACT-HISTORY table
CREATE TABLE IF NOT EXISTS ContractHistory (
                                               id SERIAL PRIMARY KEY,
                                               contractId INT NOT NULL,
                                               previousAmount DOUBLE PRECISION NOT NULL,
                                               newAmount DOUBLE PRECISION NOT NULL,
                                               changedAt TIMESTAMP NOT NULL,
                                               FOREIGN KEY (contractId) REFERENCES Contract(id)
    );

-- Create COUNTER-PARTY table
CREATE TABLE IF NOT EXISTS CounterParty (
                                            id SERIAL PRIMARY KEY,
                                            name VARCHAR(255) NOT NULL,
    counterPartySearchStrings JSON
    );

-- Create CATEGORY table
CREATE TABLE IF NOT EXISTS Category (
                                        id SERIAL PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    maxSpendingPerMonth DOUBLE PRECISION NOT NULL,
    categorySearchStrings JSON
    );


-- Create TRANSACTION table
CREATE TABLE IF NOT EXISTS Transaction (
                                           id SERIAL PRIMARY KEY,
                                           accountId INT NOT NULL,
                                           contractId INT,
                                           counterPartyId INT,
                                           date TIMESTAMP NOT NULL,
                                           amount DOUBLE PRECISION NOT NULL,
                                           amountInBankBefore DOUBLE PRECISION NOT NULL,
                                           amountInBankAfter DOUBLE PRECISION NOT NULL,
                                           categoryId INT,
                                           FOREIGN KEY (accountId) REFERENCES Account(id),
    FOREIGN KEY (contractId) REFERENCES Contract(id),
    FOREIGN KEY (counterPartyId) REFERENCES CounterParty(id),
    FOREIGN KEY (categoryId) REFERENCES Category(id)
    );