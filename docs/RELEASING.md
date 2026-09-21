# Releasing Initio

This is the exact process for cutting a new Initio release. Follow it in order — the Homebrew step in particular is easy to forget, and skipping it silently leaves `brew install` serving a stale version.

## 1. Make sure `main` is green

Confirm the latest commit on `main` has a passing test run (Actions tab, or run `./mvnw test` locally). Do not tag a red `main`.

## 2. Choose a version

Use [semantic versioning](https://semver.org/): `MAJOR.MINOR.PATCH` (e.g. `0.2.0`, `1.0.0`). No `v` prefix in this number — that gets added when you create the tag.

## 3. Bump the version in `pom.xml`

Edit the `<version>` line just below `<artifactId>initio</artifactId>` (**not** the one inside `<parent>`, which is Spring Boot's own version):

```xml
<version>0.2.0</version>
```

## 4. Commit and push

```bash
git add pom.xml
git commit -m "Bump version to 0.2.0"
git push
```

## 5. Tag and push the tag

The tag must be `v` + the exact `pom.xml` version, or the release workflow's validation step will fail on purpose.

```bash
git tag v0.2.0
git push origin v0.2.0
```

## 6. Let CI do the rest

Pushing the tag triggers `.github/workflows/release.yml`, which automatically:

1. Validates the tag is a semantic version and matches `pom.xml`
2. Runs the full test suite and builds+smoke-tests the native binary on macOS ARM64, macOS x64, Linux x64, and Windows x64
3. Packages each platform's binary into an archive and generates `SHA256SUMS`
4. Publishes a GitHub Release with all archives + `SHA256SUMS` attached

Watch it in the **Actions** tab. It takes roughly 20–30 minutes (the four platform builds run in parallel; native-image compilation is the slow part). If any platform fails, the release is not published — nothing partial goes out.

## 7. Verify the release

Once it's green, check the **Releases** page for the new version and confirm all five files are attached: four platform archives + `SHA256SUMS`.

```bash
gh release view vX.Y.Z --repo MoHussain543/Initio
```

## 8. Update the Homebrew tap — do not skip this

The formula in `MoHussain543/homebrew-tap` is **not automatically updated**. Until it's edited, `brew install` keeps serving the previous version.

```bash
git clone https://github.com/MoHussain543/homebrew-tap.git
cd homebrew-tap
```

Get the new checksums:

```bash
gh release download vX.Y.Z --repo MoHussain543/Initio --pattern "SHA256SUMS" --clobber
cat SHA256SUMS
```

Edit `Formula/initio.rb`:
- Update the three release URLs (macOS ARM64, macOS x64, Linux x64) to the new tag
- Update the three matching `sha256` values from the `SHA256SUMS` output above

Then verify locally before pushing:

```bash
brew style Formula/initio.rb
brew audit --strict MoHussain543/tap/initio   # after committing locally and re-tapping, if testing before push
```

Commit and push:

```bash
git add Formula/initio.rb
git commit -m "Update initio formula to vX.Y.Z"
git push
```

## 9. Test the updated tap for real

From a clean state (not just editing your existing install):

```bash
brew uninstall initio
brew untap MoHussain543/tap
brew install MoHussain543/tap/initio
initio --version   # should print the new version
```

## 10. Done

That's the whole process. There is no `install.sh` and no code signing yet — both are deliberately deferred (see the Phase 6 notes); Homebrew and the direct-download links in the README are the supported install paths for now.
