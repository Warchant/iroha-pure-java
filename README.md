[![Build Status](https://travis-ci.org/Warchant/iroha-pure-java.svg?branch=v1.0.0_beta-3)](https://travis-ci.org/Warchant/iroha-pure-java)
[![codecov](https://codecov.io/gh/Warchant/iroha-pure-java/branch/v1.0.0_beta-3/graph/badge.svg)](https://codecov.io/gh/Warchant/iroha-pure-java)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2d39dff34bac4bce86c628163415c962)](https://www.codacy.com/app/Warchant/iroha-pure-java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Warchant/iroha-pure-java&amp;utm_campaign=Badge_Grade)
[![](https://jitpack.io/v/warchant/iroha-pure-java.svg)](https://jitpack.io/#warchant/iroha-pure-java)


# iroha-pure-java

Only java implementation of [Iroha](https://github.com/hyperledger/iroha) Client library.

# compatibility with iroha

Compatible with `iroha-v1.0.0_beta-3` only.

# example

Transaction:

```
Transaction.builder(accountId, instant)
        .createAccount(accountName, domainId, keyPair.getPublic())
        .transferAsset(srcAccountId, dstAccountId, assetId, description, amount)
        .setAccountDetail(accountId, key, value)
        .sign(keyPair1)
        .sign(keyPair2)
        .build();
```

Query:
```
Query.builder(accountId, instant, counter)
        .getAccountAssetTransactions(accountId, assetId)
        .buildSigned(keyPair1);
```

Keypair:
```
KeyPair keypair1 = Ed25519Sha3.generateKeypair();
KeyPair keypair1 = Ed25519Sha3.generateKeypair(new byte[]{..32 bytes seed..});

```
