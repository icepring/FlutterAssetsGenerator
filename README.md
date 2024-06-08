It's a plugin that generates an asset index which we can easily find.It can be used on Android Studio or Idea.

## How to use

.Generate file

You can generate file by these ways:

- `Build` => `Generate Assets Index`
- Press `Option`(mac)/`Alt`(win) + `G`,It will generate assets.dart on lib/generated.

Simply use it like:

```dart
Image.asset(
          Assets.imageLoading,
          width: 24,
          height: 24,
          fit: BoxFit.contain,
        )
```

## Settings
### Global
`Preferences` => `Tools` => `AssetsHelper`
### Module based
You can change default settings by add following contents in your `pubspec.yaml`.

```yaml
flutter_assets_generator:
  # Optional. Sets the directory of generated localization files. Provided value should be a valid path on lib dir. Default: generated
  output_dir: generated
  # Optional. Sets whether utomatic monitoring of file changes. Default: true
  auto_detection: true
  # Optional. Sets file name conversion rules. Default: true
  named_with_parent: true
  # Optional. Sets the name for the generated localization file. Default: assets
  output_filename: assets
  # Optional. Sets the name for the generated localization class. Default: Assets
  class_name: Assets
  # Optional. Sets the filename split pattern for filename split. Default: [-_]
  filename_split_pattern: "[-_]"
  # Optional. Configuring ignore paths. Default: [],e.g: ["assets/fonts", "assets/images/dark", ...]
  path_ignore: []
```
