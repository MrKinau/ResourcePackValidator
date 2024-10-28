# Resource Pack Validator
[![GitHub issues](https://img.shields.io/github/issues/MrKinau/ResourcePackValidator)](https://github.com/MrKinau/ResourcePackValidator/issues)
[![License](https://img.shields.io/github/license/MrKinau/ResourcePackValidator)](https://github.com/MrKinau/ResourcePackValidator/blob/master/LICENSE)
[![Discord](https://img.shields.io/discord/550764567282712583?logo=discord)](https://discord.gg/xHpCDYf)

A commandline tool to validate a Minecraft Java Edition resource pack. It runs several validations, that normally run while loading the resource pack in the vanilla client as well as some extra validations to identify issues.

## Commandline Arguments
- `-help` Show all available commandline arguments
- `--resourcepack <directory or zip>`, `-rp <directory or zip>` Specifies the path of the resource pack to validate. By default, it assumes the resource pack is located at `./resourcepack` or `./resourcepack.zip` (if `./resourcepack` does not exist)
- `--verbose`, `-v` Enables verbose log output
- `-config` Specifies the path to the config file. By default, it assumes it is located at `./config.json`
- `-report <file>` Specifies the path the report file. By default, it does not generate a XML report

## Using with GitHub Action
In order to use the GitHub action, you'll need to create a workflow file inside `.github/workflows/` (e.g. `.github/workflows/validate-resource-pack.yml`) with the following content:
```yaml
name: Validate Resource Pack
on: [push]
jobs:
  validate-resource-pack:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
      - name: Validate Resource Pack
        uses: MrKinau/ResourcePackValidator@v1
        with:
          resourcepack: resourcepack
          config: config.json
```
You can change `resourcepack` with the path to your resourcepack directory or zip inside the repository.

You can change `config` to the location of your [config file](doc/CONFIG.md) inside the repo.

## Using with Gitlab CI
In order to use the Gitlab CI, you'll need to create a file inside your repository with the name `.gitlab-ci.yml` (or change the file location/name in Gitlab CI/CD Settings » General Pipelines) with the following content:
```yaml
stages:
  - validate

validate-resource-pack:
  image:
    name: ghcr.io/mrkinau/resourcepackvalidator:master
    entrypoint: [""]
  stage: validate
  script:
    - resourcepackvalidator -rp resourcepack -config config.json
```
You can change `resourcepack` with the path to your resourcepack directory or zip inside the repository.

You can change `config` to the location of your [config file](doc/CONFIG.md) inside the repo.

Additionally, you can add a JUnit like test report, which will be visible in Gitlab:
```yaml
stages:
  - validate

validate-resource-pack:
  image:
    name: ghcr.io/mrkinau/resourcepackvalidator:master
    entrypoint: [""]
  stage: validate
  script:
    - resourcepackvalidator -rp resourcepack -config config.json -report ./report.xml
  artifacts:
    when: always
    untracked: false
    name: "${CI_PROJECT_NAME}_${CI_COMMIT_REF_NAME}"
    paths:
      - "report.xml"
    expire_in: 7 day
    reports:
      junit:
        - "report.xml"
```
<details>
  <summary>Screenshot</summary>
  <img src="https://github.com/MrKinau/ResourcePackValidator/assets/13185260/20bf3697-76b3-42e5-8912-9ac39bb43dd9"  alt="Gitlab CI Tests screenshot showing some validators failing"/>
</details>

## Validators
All currently implemented Validators can be found [here](doc/VALIDATORS.md).

## Ideas / Future plans for Validators
### Extend Unused Files Detection
Extend detection, to find models which do not have an override or override a vanilla item. 
Also check all textures if they are used by a model which already is unused?

### Animated texture frames missing / too many sprite frames
Checks if an animated texture has too many / too few frames.

## Discord
To follow the project, get support or request features or bugs you can join my Discord: https://discord.gg/xHpCDYf

## Contribution
You are free to create a fork, or a pull request to participate. You also can report bugs or request a new feature in the [issues](https://github.com/MrKinau/FishingBot/issues) tab or on my [Discord](https://discord.gg/xHpCDYf) (I will answer them as soon as possible)
