# Diagrams Documentation

This directory contains Mermaid diagram source files (`.mmd`) and their generated images (`.png`) used throughout the project documentation.

## Available Diagrams

- **`three-tier-architecture.mmd`** - Three-tier architecture diagram showing Presentation, Business Logic, and Data layers
- **`database-schema.mmd`** - Entity-Relationship Diagram (ERD) showing database tables (products, users) and their relationships
- **`system-architecture.mmd`** - System architecture diagram showing components and their interactions
- **`auth-flow.mmd`** - Authentication flow diagram
- **`role-access.mmd`** - Role-based access control diagram

## For Contributors: Updating Diagrams

### Prerequisites

Install Mermaid CLI:
```bash
npm install -g @mermaid-js/mermaid-cli
```

### Regenerating Diagrams

When you modify a Mermaid source file (`.mmd`), regenerate the corresponding PNG image:

**Generate PNG from Mermaid source:**
```bash
# Three-tier architecture
mmdc -i docs/diagrams/three-tier-architecture.mmd -o docs/diagrams/three-tier-architecture.png

# Database schema (includes products and users tables)
mmdc -i docs/diagrams/database-schema.mmd -o docs/diagrams/database-schema.png

# System architecture
mmdc -i docs/diagrams/system-architecture.mmd -o docs/diagrams/system-architecture.png

# Authentication flow
mmdc -i docs/diagrams/auth-flow.mmd -o docs/diagrams/auth-flow.png

# Role-based access control
mmdc -i docs/diagrams/role-access.mmd -o docs/diagrams/role-access.png
```

**Generate SVG format (optional, for better quality):**
```bash
mmdc -i docs/diagrams/system-architecture.mmd -o docs/diagrams/system-architecture.svg
```

### Best Practices

1. **Always update the source file first** - Edit the `.mmd` file, not the generated `.png`
2. **Regenerate images after changes** - After modifying `.mmd` files, regenerate the corresponding images
3. **Commit both files** - Commit both the `.mmd` source and the generated `.png` image
4. **Test the diagram** - Use [Mermaid Live Editor](https://mermaid.live/) to preview your changes before generating

### Where Diagrams Are Used

- **`TECHNICAL-ANALYSIS-REPORT.md`** - Uses three-tier architecture and database schema diagrams
- **`README.md`** - Uses system architecture, authentication flow, and role-based access diagrams

### Mermaid Syntax Reference

- **Flowcharts**: `graph TB` or `graph LR` for top-bottom or left-right layouts
- **ER Diagrams**: `erDiagram` for database schema visualization
- **Sequence Diagrams**: `sequenceDiagram` for interaction flows
- **Class Diagrams**: `classDiagram` for UML class diagrams

For more information, see [Mermaid Documentation](https://mermaid.js.org/).

## Diagram Update Checklist

When updating a diagram:

- [ ] Edit the `.mmd` source file
- [ ] Preview changes in Mermaid Live Editor
- [ ] Regenerate the `.png` image using Mermaid CLI
- [ ] Verify the image looks correct
- [ ] Update any documentation that references the diagram if needed
- [ ] Commit both `.mmd` and `.png` files

