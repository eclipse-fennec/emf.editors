# GitHub CI

The repository runs two GitHub Actions workflows: a build matrix on every
push and pull request, and a license-header check on the protected branches.
There is no GitHub-driven publishing pipeline — P2 update sites are produced
on a separate Jenkins instance referenced from the README.

All workflow definitions live in [`.github/workflows`](../.github/workflows).

## Branch model

`snapshot` is the active development line — all PRs target it. `main` always
holds the latest released version. Unlike the other Fennec repositories this
project ships an **Eclipse P2 update site** rather than Maven Central
artifacts; the release update site is produced from `main` by Jenkins
(see [Published artifacts](#published-artifacts)).

| Branch     | Purpose                                            |
|------------|----------------------------------------------------|
| `snapshot` | Active development. PRs target this branch.       |
| `main`     | Latest release — code here matches the released P2 update site. |

## `build.yml` — CI Build

* **File:** [`.github/workflows/build.yml`](../.github/workflows/build.yml)
* **Triggers:** every `push` and `pull_request` — except changes restricted
  to `docs/**` or `.github/**/*docs*`/`*codeql*`/top-level `.github/*.yml`
  (the path filters skip CI for docs-only commits).
* **Purpose:** Verify the source tree compiles and tests pass on both
  Linux and Windows.
* **Matrix:**
  * OS: `ubuntu-latest`, `windows-latest`
  * Java: `17` (Temurin)
  * UI tests run under `xvfb-run --auto-servernum` on Linux (the matrix
    `runner` value wraps the command, so SWT-based tests have a virtual
    display).
* **Steps:** checkout → Gradle wrapper validation → set up JDK with Gradle
  cache → `./gradlew build` → upload test reports as artifacts named
  `JDK17_<os>-test-reports`.
* **Secrets used:** none.

A green run on both OSes is the gating signal for review.

## `license.yml` — License header check

* **File:** [`.github/workflows/license.yml`](../.github/workflows/license.yml)
* **Triggers:** `push` and `pull_request` on `main` or `snapshot`, plus
  manual `workflow_dispatch`. Topic branches are *not* checked until the PR
  is opened.
* **Purpose:** Verify every source file carries the Eclipse Public License
  2.0 header. Uses [apache/skywalking-eyes](https://github.com/apache/skywalking-eyes)
  driven by [`.licenserc.yaml`](../.licenserc.yaml).
* **What it checks:** the SPDX header pattern declared in `.licenserc.yaml`,
  applied to every file *not* listed under `paths-ignore`.
* **Failure mode:** on a PR the action comments on the offending lines via
  `GITHUB_TOKEN`. The fix is to add the standard header (template in
  [`CONTRIBUTING.md`](../CONTRIBUTING.md#license-headers)) and push again.
* **Note:** the action is pinned to `apache/skywalking-eyes@main`. For
  reproducibility, consider pinning to a specific release tag.

## Publishing

This repository does **not** publish artifacts via GitHub Actions. The
P2 update site is produced by a Jenkins job at `devel.data-in-motion.biz`.

## Published artifacts

This repository ships an **Eclipse P2 update site**, not Maven artifacts.

| Channel    | Where to find it                                                                                                                                                                                                 | Produced by                                          |
|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| Snapshot   | `jar:https://devel.data-in-motion.biz/jenkins/job/Eclipse-Fennec/job/emf.editors/job/snapshot/lastSuccessfulBuild/artifact/org.eclipse.fennec.emf.editor.reflective.xmi/generated/p2updatesite.zip!/` | Jenkins `Eclipse-Fennec / emf.editors / snapshot`   |
| Manual     | Download the `p2updatesite.zip` from the same Jenkins job and install locally via *Help → Install New Software → Add → Local...*                                                                                  | as above                                             |

Installation steps and the full Eclipse update-site URL are documented in
the [README](../README.md#installation).

## Reproducing CI locally

* Full PR build:
  ```bash
  ./gradlew clean build
  ```
* License headers:
  ```bash
  docker run --rm -v $(pwd):/github/workspace \
    ghcr.io/apache/skywalking-eyes/license-eye header check
  ```
