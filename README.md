# Blockchain Messenger

Blockchain messenger is a Blockchain-based messaging application
implemented using Scala and the Akka Framework's Actor model.

## Overview

- The blockchain public distributed leger in blockchain-messenger is a record of all sent messages, rather then monetary
  transfers
- To be a participant in messaging, you must be a node hosting the blockchain
- Sending a message costs tokens, mining a block earns tokens
- Each node has a private and public key. Messages stored on the blockchain are encrypted with the recipient's public
  key.
- Messages where the given node is a recipient can be decrypted with that node's private key.

## API

### status

#### GET

Returns the status of the blockchain.

`GET {{base_url}}/status`

<details>
<summary> Example Response </summary>

```json
{
  "blocks": [
    {
      "hash": "f51545fd7e6462d968414194ff8b687e62cb8d51ced26de56c316fec6820e026",
      "index": 0,
      "timestamp": 0
    },
    {
      "hash": "fbca74407a8a7edb2c277e8f2148e435697a933f75188b4f124befd1545dc3c9",
      "index": 1,
      "proof": 28082,
      "timestamp": 1713188892367,
      "transactions": [
        {
          "beneficiary": "node1",
          "originator": "theBank",
          "transactionId": "eSP6QF",
          "value": 10
        }
      ]
    }
  ]
}
```

</details>

### transactions

#### GET
Returns the pending transactions to be mined.

`GET {{base_url}}/transactions`

<details>
<summary> Example Response </summary>

```json
[
  {
    "beneficiary": "node2",
    "message": "Nz0tjK0FrRvnH9HI6gs/H+hXf6Q4qxIElSCB8O4CJ9sTngWYyzH5YbEngE02ImI3KypBRyjU5DYM9zCRBbDrWIJjuThV5W9CahdfvGR/sqifATWIWAQcujdRvEILPgGK1Wh1EkukFBTkx4PMx4UZOY31gFIrE/w0M97g/PbTwAk=",
    "originator": "node1",
    "transactionId": "6txU0I",
    "value": 1
  }
]
```

</details>

#### POST

Add messsage pending transactions to be mined.

`GET {{base_url}}/transactions`

<details>
<summary> Example Response </summary>

```json
[
  {
    "beneficiary": "node2",
    "message": "Nz0tjK0FrRvnH9HI6gs/H+hXf6Q4qxIElSCB8O4CJ9sTngWYyzH5YbEngE02ImI3KypBRyjU5DYM9zCRBbDrWIJjuThV5W9CahdfvGR/sqifATWIWAQcujdRvEILPgGK1Wh1EkukFBTkx4PMx4UZOY31gFIrE/w0M97g/PbTwAk=",
    "originator": "node1",
    "transactionId": "6txU0I",
    "value": 1
  }
]
```

</details>

### mine
### messages
### balance