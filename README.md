[![Build Status](https://travis-ci.org/Warchant/iroha-pure-java.svg?branch=master)](https://travis-ci.org/Warchant/iroha-pure-java)
[![codecov](https://codecov.io/gh/Warchant/iroha-pure-java/branch/master/graph/badge.svg)](https://codecov.io/gh/Warchant/iroha-pure-java)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2d39dff34bac4bce86c628163415c962)](https://www.codacy.com/app/Warchant/iroha-pure-java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Warchant/iroha-pure-java&amp;utm_campaign=Badge_Grade)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FWarchant%2Firoha-pure-java.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FWarchant%2Firoha-pure-java?ref=badge_shield)
[![](https://jitpack.io/v/warchant/iroha-pure-java.svg)](https://jitpack.io/#warchant/iroha-pure-java)


# iroha-pure-java

Only java implementation of [Iroha](https://github.com/hyperledger/iroha) Client library.

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


## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FWarchant%2Firoha-pure-java.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2FWarchant%2Firoha-pure-java?ref=badge_large)