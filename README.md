# Crossmint Wallet Java Demo

Java console app demonstrating Crossmint wallet operations with EVM keypair signer.

## What it does
1. Generate EVM keypair using web3j
2. Create smart wallet via Crossmint API with external wallet signer
3. Fund wallet with test USDC tokens
4. Create USDC transfer transaction
5. Sign the transaction message hash with EVM keypair
6. Approve transaction with signature
7. Poll transaction status until completion

## Project Structure
```
src/
├── Main.java              # Main demo orchestrator
├── services/
│   ├── KeypairService.java    # EVM keypair generation and signing
│   └── CrossmintService.java  # Crossmint API client
└── utils/
    ├── Config.java            # Configuration management
    └── Logger.java             # Logging utilities
```

## Setup

### Prerequisites
- Java 17+ (tested with Java 21)
- Gradle (or use the included wrapper)

### Environment
Create a `.env` file in the project root and add your API key and recipient wallet:
```bash
# Crossmint API Configuration
CROSSMINT_API_KEY=your_crossmint_api_key_here
CROSSMINT_BASE_URL=https://staging.crossmint.com

# Network Configuration
NETWORK=base-sepolia

# Logging Configuration
LOG_LEVEL=info

# Demo Configuration
DEMO_RECIPIENT_ADDRESS=0x6671f7552df0fbAF762Bd40aEd1cA3ec670d6161
DEMO_AMOUNT_USDC=1
FUND_AMOUNT=10
```

### Run
```bash
./gradlew run
```

Or via IDE: run `Main.java`

## API Endpoints Used

### 1. Create Wallet
- **POST** `/api/2025-06-09/wallets`
- Creates a smart wallet with external wallet admin signer

### 2. Fund Wallet
- **POST** `/api/v1-alpha2/wallets/{walletLocator}/balances`
- Funds wallet with test USDC tokens

### 3. Create Transaction
- **POST** `/api/2025-06-09/wallets/{walletLocator}/tokens/{chain}:usdc/transfers`
- Creates USDC transfer transaction

### 4. Approve Transaction
- **POST** `/api/2025-06-09/wallets/{walletLocator}/transactions/{transactionId}/approvals`
- Approves transaction with EVM signature

### 5. Get Transaction Status
- **GET** `/api/2025-06-09/wallets/{walletLocator}/transactions/{transactionId}`
- Polls transaction status

## Dependencies
- **web3j**: EVM keypair generation and message signing
- **OkHttp**: HTTP client for API calls
- **Jackson**: JSON processing
- **dotenv-java**: Environment variable loading
- **SLF4J**: Logging

## Key Features
- **Real API Integration**: Uses actual Crossmint staging API endpoints
- **Proper Message Signing**: Signs transaction message hashes correctly
- **Error Handling**: Comprehensive error handling and logging
- **Clean Architecture**: Simple, maintainable code structure
- **Production Ready**: Easy to integrate into existing Java backends

## Integration Notes
- Replace `CROSSMINT_API_KEY` with your actual Crossmint API key
- The demo uses `base-sepolia` testnet by default
- Message signing uses the exact hash provided by Crossmint API
- All API calls include proper error handling and logging