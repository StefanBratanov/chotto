# Chotto

[![build](https://github.com/StefanBratanov/chotto/actions/workflows/build.yml/badge.svg)](https://github.com/StefanBratanov/chotto/actions/workflows/build.yml)

> ⚠️ This project is still WIP.

Chotto can be used to participate in the KZG Ceremony which will
bring [EIP-4844](https://www.eip4844.com/) to life. It is an Ethereum's Powers of Tau
(PoT) client implementation written in Java. It follows the spec defined in
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

`entropy-entry` is a required value to the process. It would be used as a seed to generate
random secrets in the background. There will be several layers of randomness on top of
this text, so there is no need to worry about its uniqueness or keeping it safe.

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
Usage: chotto [-hV] [--sign-contributions]
              [--auth-callback-endpoint=<authCallbackEndpoint>]
              [--authentication=<provider>]
              [--contribution-attempt-period=<contributionAttemptPeriod>]
              --entropy-entry=<entropyEntry> --sequencer=<sequencer>
              [--server-port=<serverPort>]
Ethereum's Power of Tau client implementation written in Java
      --auth-callback-endpoint=<authCallbackEndpoint>
                             The URL of this process which will be used as an
                               authentication callback endpoint. Specify this
                               option ONLY if you decide to login from a
                               browser on a different computer. Make sure the
                               URL is accessible from that browser.
      --authentication=<provider>
                             The authentication provider which will be used for
                               logging in. Valid values: Ethereum, Github
                               Default: Ethereum
      --contribution-attempt-period=<contributionAttemptPeriod>
                             How often (in seconds) to attempt contribution
                               once authenticated
                               Default: 3
      --entropy-entry=<entropyEntry>
                             A text which would be used as a seed to generate
                               random secrets in the background. There will be
                               several layers of randomness on top of this
                               text, so there is no need to worry about its
                               uniqueness or keeping it safe.
  -h, --help                 Show this help message and exit.
      --sequencer=<sequencer>
                             The URL of the sequencer which would be used for
                               the ceremony
      --server-port=<serverPort>
                             The port on which to start the local server
                               Default: 8080
      --sign-contributions   Sign your contributions with your identity. Doing
                               so is RECOMMENDED.
                               Default: true
  -V, --version              Print version information and exit.
```
