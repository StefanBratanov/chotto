# Chotto

[![build](https://github.com/StefanBratanov/chotto/actions/workflows/build.yml/badge.svg)](https://github.com/StefanBratanov/chotto/actions/workflows/build.yml)
[![codecov](https://codecov.io/github/StefanBratanov/chotto/branch/master/graph/badge.svg?token=9WEPEA6GA7)](https://codecov.io/github/StefanBratanov/chotto)
[![GitHub license](https://img.shields.io/github/license/StefanBratanov/chotto.svg)](https://github.com/StefanBratanov/chotto/blob/master/LICENSE)

> ⚠️ This project is still WIP.

Chotto can be used to participate in the KZG Ceremony which will
bring [EIP-4844](https://www.eip4844.com/) to life. It is an Ethereum's Powers of Tau
(PoT) client CLI implementation written in Java. It follows the spec defined in
the [KZG ceremony specs](https://github.com/ethereum/kzg-ceremony-specs).

## Build Instructions

### Install Prerequisites

- Java 11+

### Build and Dist

```bash
git clone https://github.com/StefanBratanov/chotto.git
cd chotto
./gradlew installDist
```

This will install ready to use executables in `build/install/chotto/bin`

## Usage

`entropy-entry` is a required value to the process. It would be used as a seed to generate random
secrets in the background. There will be several layers of randomness on top of this text, so there
is no need to worry about its uniqueness or keeping it safe.

### Sample Usage

#### Authenticate with Ethereum

```bash
./chotto --sequencer=https://kzg-ceremony-sequencer-dev.fly.dev/ --entropy-entry="Ethereum is awesome"
```

#### Authenticate with GitHub

```bash
./chotto --sequencer=https://kzg-ceremony-sequencer-dev.fly.dev/ --entropy-entry="Ethereum is awesome" --authentication=github
```

### CLI arguments

```bash
$ ./chotto --help
  _____ _           _   _
 / ____| |         | | | |
| |    | |__   ___ | |_| |_ ___
| |    | '_ \ / _ \| __| __/ _ \
| |____| | | | (_) | |_| || (_) |
 \_____|_| |_|\___/ \__|\__\___/
Usage: chotto [-hV] [--bls-sign-contributions]
              [--ecdsa-sign-batch-contribution] [--authentication=<provider>]
              [--callback-endpoint=<callbackEndpoint>]
              [--contribution-attempt-period=<contributionAttemptPeriod>]
              --entropy-entry=<entropyEntry>
              [--output-directory=<outputDirectory>] --sequencer=<sequencer>
              [--server-port=<serverPort>]
Ethereum's Power of Tau client implementation written in Java
      --authentication=<provider>
                  The authentication provider which will be used for logging
                    in. Valid values: Ethereum, Github
                    Default: Ethereum
      --bls-sign-contributions
                  Sign your contributions with your identity. Doing so is
                    RECOMMENDED.
                    Default: true
      --callback-endpoint=<callbackEndpoint>
                  The URL of the server which is started by this process.
                    Specify this option ONLY if you decide to login and sign
                    from a browser on a different computer. Make sure the URL
                    is accessible from that browser.
      --contribution-attempt-period=<contributionAttemptPeriod>
                  How often (in seconds) to attempt contribution once
                    authenticated. This value could change dynamically based on
                    responses from the sequencer.
                    Default: 15
      --ecdsa-sign-batch-contribution
                  Sign the batch contribution with your Ethereum address. Doing
                    so is RECOMMENDED. This value is only applicable when the
                    user has authenticated with Ethereum.
                    Default: true
      --entropy-entry=<entropyEntry>
                  A text which would be used as a seed to generate random
                    secrets in the background. There will be several layers of
                    randomness on top of this text, so there is no need to
                    worry about its uniqueness or keeping it safe.
  -h, --help      Show this help message and exit.
      --output-directory=<outputDirectory>
                  The directory where the outputs of the ceremony will be stored
                    Default: <user.home>\kzg-ceremony
      --sequencer=<sequencer>
                  The URL of the sequencer which would be used for the ceremony
      --server-port=<serverPort>
                  The port on which to start the local server
                    Default: 8080
  -V, --version   Print version information and exit.
```
