# Chotto

[![build](https://github.com/StefanBratanov/chotto/actions/workflows/build.yml/badge.svg)](https://github.com/StefanBratanov/chotto/actions/workflows/build.yml)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/StefanBratanov/chotto)](https://github.com/StefanBratanov/chotto/releases/latest)
[![GitHub license](https://img.shields.io/github/license/StefanBratanov/chotto.svg)](https://github.com/StefanBratanov/chotto/blob/master/LICENSE)
[![codecov](https://codecov.io/github/StefanBratanov/chotto/branch/master/graph/badge.svg?token=9WEPEA6GA7)](https://codecov.io/github/StefanBratanov/chotto)
[![CodeFactor](https://www.codefactor.io/repository/github/stefanbratanov/chotto/badge)](https://www.codefactor.io/repository/github/stefanbratanov/chotto)

Chotto can be used to participate in the KZG Ceremony which will
bring [EIP-4844](https://www.eip4844.com/) to life. It is an Ethereum's Powers of Tau
(PoT) client CLI implementation written in Java. It follows the spec defined in
the [KZG ceremony specs](https://github.com/ethereum/kzg-ceremony-specs).

🕯️🕯️🕯️ **The KZG Ceremony is live** and the url of the `sequencer`
is https://seq.ceremony.ethereum.org. Check
the [Install Instructions](#install-instructions) and [Usage](#usage) to see how to use Chotto
to contribute. After contributing, you can check your
contribution [here](https://ceremony.ethereum.org/#/record). 🕯️🕯️🕯️

## Install Instructions

### Install Prerequisites

- Java 11+

### Binary Releases

Binary releases are available from
the [releases page](https://github.com/StefanBratanov/chotto/releases).

Once downloaded and unzipped, executables will be available in the `bin/` folder.

### Manual Build

You can also manually build the code:

```bash
git clone https://github.com/StefanBratanov/chotto.git
cd chotto
./gradlew installDist
```

This will install ready to use executables in the `build/install/chotto/bin` folder.

## Generating randomness

Generating randomness is an important part of the KZG Ceremony. Each participant needs to generate 4
different random secrets. The user should not have a knowledge of these generated values and also
all secrets should be wiped out from memory after the contribution is complete. The way each random
secret is generated in Chotto is as follows:

* User provides `entropy-entry` argument of a text with at least 6 characters.
* A seed with 256 bytes is initialised with the `entropy-entry` bytes. (truncated or padded with
  zeros)
* Half or more bytes (128 or 256 - `entropy-entry` length) are replaced by random bytes. (based on
  `java.util.Random`)
* The seed is passed to a `BLS KeyGen` function which adds more randomness and ultimately generates
  the secret.

The secrets will only live in the Java process, so won't be exposed when the browser is opened for
logging in and signing the contribution. After the
Java process is terminated, all secrets will be wiped out from memory.

## Usage

Required arguments:

* `entropy-entry` (see [Generating randomness](#generating-randomness) for more information)
* `sequencer`

See [CLI Arguments](#cli-arguments) for all available arguments.

### Sample Usage

Note: For Windows, use the `chotto.bat` executable.

#### Authenticate with Ethereum

```bash
./chotto --sequencer=https://seq.ceremony.ethereum.org/ --entropy-entry="Ethereum is awesome"
```

#### Authenticate with GitHub

```bash
./chotto --sequencer=https://seq.ceremony.ethereum.org/ --entropy-entry="Ethereum is awesome" --authentication=github
```

#### Run against a local sequencer

```bash
./chotto --sequencer=http://localhost:3000/ --entropy-entry="Ethereum is awesome"
```

You can start a local sequencer by following the setup instructions
at [KZG Ceremony Rest API](https://github.com/ethereum/kzg-ceremony-sequencer).

### CLI arguments

```bash
$ ./chotto --help
  _____ _           _   _
 / ____| |         | | | |
| |    | |__   ___ | |_| |_ ___
| |    | '_ \ / _ \| __| __/ _ \
| |____| | | | (_) | |_| || (_) |
 \_____|_| |_|\___/ \__|\__\___/
Usage: chotto [-hV] [--bls-sign-sub-contributions] [--ecdsa-sign-contribution]
              [--validate-receipt] [--verify-transcript]
              [--authentication=<provider>]
              [--callback-endpoint=<callbackEndpoint>]
              [--contribution-attempt-period=<contributionAttemptPeriod>]
              --entropy-entry=<entropyEntry>
              [--output-directory=<outputDirectory>] --sequencer=<sequencer>
              [--server-port=<serverPort>]
Ethereum's Power of Tau client implementation written in Java
      --authentication=<provider>
                            The authentication provider which will be used for
                              logging in. Valid values: Ethereum, Github
                              Default: Ethereum
      --bls-sign-sub-contributions
                            Sign your sub-contributions using your identity.
                              Doing so is RECOMMENDED.
                              Default: true
      --callback-endpoint=<callbackEndpoint>
                            The URL of the server which is started by this
                              process. Specify this option ONLY if you decide
                              to login and sign from a browser on a different
                              computer. Make sure the URL is accessible from
                              that browser.
      --contribution-attempt-period=<contributionAttemptPeriod>
                            How often (in seconds) to attempt contribution once
                              authenticated
                              Default: 30
      --ecdsa-sign-contribution
                            Sign your contribution using the Ethereum address
                              you logged in with. Doing so is RECOMMENDED. This
                              value is only applicable when you have
                              authenticated with Ethereum.
                              Default: true
      --entropy-entry=<entropyEntry>
                            A text which would be used as a seed to generate
                              random secrets in the background. There will be
                              several layers of randomness on top of this text,
                              so there is no need to worry about its uniqueness
                              or keeping it a secret.
  -h, --help                Show this help message and exit.
      --output-directory=<outputDirectory>
                            The directory where the outputs of the ceremony
                              will be saved
                              Default: <user.home>\kzg-ceremony
      --sequencer=<sequencer>
                            The URL of the sequencer which would be used for
                              the ceremony
      --server-port=<serverPort>
                            The port on which to start the local server
                              Default: 8080
  -V, --version             Print version information and exit.
      --validate-receipt    Whether to validate the receipt after contributing
                              against the sequencer transcript or not
                              Default: false
      --verify-transcript   Whether to verify the sequencer transcript at
                              startup or not
                              Default: false
```
