# Meta Monjurul

A Compose for Desktop application that automates metadata generation for stock marketplaces (Adobe Stock, Pond5, Shutterstock, Freepik) using Google's Gemini AI.

## Features

- **File Import**: Drag & drop SVG, EPS, PNG, JPG, WEBP files
- **Preview Generation**: High-quality preview rendering with glassmorphism aesthetics
- **AI-Powered Metadata**: Google Gemini API integration for automatic title, description, and keyword generation
- **Multi-Marketplace Export**: CSV export formatted for Adobe Stock, Pond5, Shutterstock, and Freepik
- **Glassmorphism UI**: Beautiful dark theme with golden/yellow gradients and translucent effects

## Design Theme

The application features a high-quality scalable vector graphic aesthetic with:
- Translucent geometric shapes
- Dynamic yellow and golden gradients
- Soft refractive lighting
- Realistic blur effects
- Perfect for commercial stock marketplace visuals

## Supported Marketplaces

| Marketplace | CSV Format | Key Fields |
|-------------|------------|------------|
| Adobe Stock | Filename, Title, Keywords, Category, Releases | Category mapping (1-11) |
| Pond5 | Clipid, OriginalFilename, Copyright, Price, name, Keywords, Description | Extended metadata |
| Shutterstock | Filename, Description, Keywords, Categories, Editorial, Mature content, illustration | Category tags |
| Freepik | Filename, Title, Tags, Category | Simplified format |

## Requirements

- Java 21+
- Gradle 8.7+ (wrapper included)
- Google Gemini API key (get from [Google AI Studio](https://aistudio.google.com))

## Building

```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Create native distributables
./gradlew packageDmg    # macOS
./gradlew packageMsi    # Windows
./gradlew packageDeb    # Linux
```

## Usage

1. **Launch** the application
2. **Configure** your Gemini API key in Settings (⚙️)
3. **Import** design files by dragging them into the drop zone
4. **Preview** renders automatically for each asset
5. **Generate Metadata** - Click "Generate with AI" for each asset
6. **Review & Edit** - Modify titles, descriptions, keywords as needed
7. **Export** - Click "Export CSV" and select target marketplaces
8. **Upload** - Use generated CSV files with each marketplace's bulk upload tool

## Project Structure

```
src/main/kotlin/com/metamonjurul/
├── Main.kt                    # Application entry point
├── model/
│   └── StockAsset.kt          # Data models
├── service/
│   ├── FileImportService.kt   # File handling
│   ├── PreviewGenerationService.kt  # Preview rendering
│   ├── GeminiService.kt       # AI metadata generation
│   ├── CsvExportService.kt    # Marketplace CSV export
│   └── SettingsService.kt     # Persistent settings
└── ui/
    ├── Theme.kt               # Glassmorphism color scheme
    ├── Typography.kt          # Custom typography
    ├── components/
    │   └── GlassComponents.kt # Reusable glassmorphism components
    ├── screens/
    │   ├── AssetScreens.kt    # Asset list & detail views
    │   └── ExportAndSettingsScreens.kt
    └── MainWindow.kt          # Main window with navigation
```

## Configuration

Settings are stored in `~/.metamonjurul/settings.json`:
- Gemini API key
- Default export directory
- Preferred marketplaces
- Preview quality settings

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl+O` | Import files |
| `Ctrl+E` | Export CSV |
| `Ctrl+,` | Open Settings |
| `Delete` | Remove selected asset |

## License

MIT License - Feel free to use for commercial and personal projects.

## Credits

- Built with [Compose for Desktop](https://github.com/JetBrains/compose-multiplatform)
- AI powered by [Google Gemini](https://ai.google.dev/)
- Icons from [Material Design Icons](https://fonts.google.com/icons)