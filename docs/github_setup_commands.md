# GitHub Repository Setup Commands

This document provides the necessary commands to create and link a GitHub repository for the Collectibles Store API project.

## Prerequisites

- GitHub account
- Git installed on your system
- SSH key configured with GitHub (recommended) or use HTTPS

## Step 1: Create GitHub Repository

### Option A: Using GitHub CLI (Recommended)

```bash
# Install GitHub CLI if not already installed
# Windows: winget install GitHub.cli
# macOS: brew install gh
# Linux: curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg

# Login to GitHub
gh auth login

# Create repository
gh repo create collectibles-store --public --description "A RESTful API for managing collectible items using Java and the Spark framework" --clone

# Navigate to the repository
cd collectibles-store
```

### Option B: Using GitHub Web Interface

1. Go to [GitHub.com](https://github.com)
2. Click the "+" icon in the top right corner
3. Select "New repository"
4. Repository name: `collectibles-store`
5. Description: `A RESTful API for managing collectible items using Java and the Spark framework`
6. Set visibility to Public
7. **Do NOT** initialize with README, .gitignore, or license (we already have these)
8. Click "Create repository"

## Step 2: Initialize Local Repository

```bash
# Navigate to your project directory
cd "C:\Users\USER\Desktop\Java Spark"

# Initialize git repository
git init

# Add all files to staging
git add .

# Create initial commit
git commit -m "Initial commit: Java Spark Collectibles Store API

- Implemented RESTful API with user CRUD operations
- Added Maven configuration with Spark, Logback, and Gson dependencies
- Created comprehensive user management system
- Added proper error handling and validation
- Implemented CORS support for web clients
- Added comprehensive documentation and README"
```

## Step 3: Link Local Repository to GitHub

### Option A: If you used GitHub CLI (Repository already cloned)

```bash
# The repository is already cloned and linked
# Just copy your files to the cloned directory
cp -r "C:\Users\USER\Desktop\Java Spark\*" .
git add .
git commit -m "Add project files"
git push origin main
```

### Option B: If you created repository via web interface

```bash
# Add remote origin
git remote add origin https://github.com/YOUR_USERNAME/collectibles-store.git

# Rename default branch to main (if needed)
git branch -M main

# Push to GitHub
git push -u origin main
```

## Step 4: Verify Repository Setup

```bash
# Check remote configuration
git remote -v

# Check branch status
git branch -a

# Check repository status
git status
```

## Step 5: Set Up Branch Protection (Optional but Recommended)

```bash
# Create development branch
git checkout -b development
git push -u origin development

# Create feature branch template
git checkout -b feature/template
git push -u origin feature/template
git checkout main
git branch -d feature/template
git push origin --delete feature/template
```

## Step 6: Configure Repository Settings

### Using GitHub CLI:

```bash
# Enable issues
gh repo edit --enable-issues

# Enable discussions
gh repo edit --enable-discussions

# Set default branch to main
gh repo edit --default-branch main

# Add topics/tags
gh repo edit --add-topic java --add-topic spark --add-topic rest-api --add-topic maven --add-topic collectibles
```

### Using GitHub Web Interface:

1. Go to your repository settings
2. Enable Issues and Discussions
3. Set default branch to `main`
4. Add topics: `java`, `spark`, `rest-api`, `maven`, `collectibles`
5. Configure branch protection rules if needed

## Step 7: Create Additional Repository Files

```bash
# Create .gitignore for Java projects
cat > .gitignore << 'EOF'
# Compiled class file
*.class

# Log file
*.log

# BlueJ files
*.ctxt

# Mobile Tools for Java (J2ME)
.mtj.tmp/

# Package Files
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# Virtual machine crash logs
hs_err_pid*
replay_pid*

# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iws
*.iml
*.ipr
.vscode/
.settings/
.project
.classpath

# OS
.DS_Store
Thumbs.db

# Application specific
logs/
*.log
application.properties
application-*.properties
EOF

# Create LICENSE file
cat > LICENSE << 'EOF'
MIT License

Copyright (c) 2024 Collectibles Store API

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
EOF

# Add and commit new files
git add .gitignore LICENSE
git commit -m "Add .gitignore and LICENSE files"
git push origin main
```

## Step 8: Create GitHub Actions Workflow (Optional)

```bash
# Create GitHub Actions directory
mkdir -p .github/workflows

# Create CI/CD workflow
cat > .github/workflows/ci.yml << 'EOF'
name: CI/CD Pipeline

on:
  push:
    branches: [ main, development ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Run tests
      run: mvn test
      
    - name: Build project
      run: mvn clean package
      
    - name: Upload build artifacts
      uses: actions/upload-artifact@v3
      with:
        name: jar-file
        path: target/*.jar
EOF

# Add and commit workflow
git add .github/
git commit -m "Add GitHub Actions CI/CD workflow"
git push origin main
```

## Step 9: Create Project Documentation

```bash
# Create CONTRIBUTING.md
cat > CONTRIBUTING.md << 'EOF'
# Contributing to Collectibles Store API

Thank you for your interest in contributing to the Collectibles Store API project!

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a feature branch
4. Make your changes
5. Test your changes
6. Submit a pull request

## Development Guidelines

- Follow Java coding standards
- Write tests for new features
- Update documentation as needed
- Use meaningful commit messages

## Pull Request Process

1. Ensure your code follows the project's coding standards
2. Add tests for any new functionality
3. Update documentation if necessary
4. Submit a pull request with a clear description
EOF

# Create CHANGELOG.md
cat > CHANGELOG.md << 'EOF'
# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2024-01-15

### Added
- Initial release of Collectibles Store API
- User CRUD operations (Create, Read, Update, Delete)
- RESTful API endpoints
- Maven build configuration
- Comprehensive documentation
- Error handling and validation
- CORS support
- Logging with Logback

### Features
- GET /users - Retrieve all users
- GET /users/:id - Retrieve user by ID
- POST /users/:id - Create new user
- PUT /users/:id - Update existing user
- DELETE /users/:id - Delete user
- OPTIONS /users/:id - Check if user exists
- GET /users/search - Search users
- GET /users/role/:role - Get users by role
- GET /users/stats - Get user statistics
EOF

# Add and commit documentation
git add CONTRIBUTING.md CHANGELOG.md
git commit -m "Add project documentation files"
git push origin main
```

## Step 10: Final Verification

```bash
# Check repository status
git status

# View commit history
git log --oneline

# Check remote configuration
git remote -v

# Verify all files are tracked
git ls-files
```

## Repository URL

Your repository will be available at:
`https://github.com/YOUR_USERNAME/collectibles-store`

## Next Steps

1. **Clone the repository** on other machines:
   ```bash
   git clone https://github.com/YOUR_USERNAME/collectibles-store.git
   ```

2. **Set up development environment**:
   ```bash
   cd collectibles-store
   mvn clean compile
   mvn test
   ```

3. **Start development**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.spark.collectibles.Application"
   ```

4. **Create issues and milestones** for project tracking

5. **Set up branch protection rules** for main branch

6. **Configure webhooks** for CI/CD integration

## Troubleshooting

### Common Issues

1. **Authentication failed**: Make sure your SSH key is configured or use HTTPS with token
2. **Repository already exists**: Choose a different name or delete the existing repository
3. **Permission denied**: Check your GitHub permissions and repository access
4. **Branch not found**: Make sure you're pushing to the correct branch name

### Useful Commands

```bash
# Check Git configuration
git config --list

# Check SSH connection
ssh -T git@github.com

# Reset remote URL if needed
git remote set-url origin https://github.com/YOUR_USERNAME/collectibles-store.git

# Force push if needed (use with caution)
git push --force-with-lease origin main
```

## Repository Management

### Regular Maintenance

1. **Keep dependencies updated**:
   ```bash
   mvn versions:display-dependency-updates
   mvn versions:use-latest-versions
   ```

2. **Clean up branches**:
   ```bash
   git branch -d feature-branch
   git push origin --delete feature-branch
   ```

3. **Update documentation** as the project evolves

4. **Monitor issues and pull requests** regularly

This setup provides a complete GitHub repository with proper structure, documentation, and CI/CD pipeline for the Collectibles Store API project.
