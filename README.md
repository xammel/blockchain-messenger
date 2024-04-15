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

## Quick Start

The following instructions will run a network of 3 nodes in a docker container. It will run the version of code within this repository on `main`.

- Clone this repository
- Run the following command in the root of the repository: 
```bash 
docker build --build-arg SBT_VERSION="1.2.7" --build-arg SSH_PRIVATE_KEY="$(cat ./docker/repo-key)" -t xammel/blockchain-messenger .
```
- Run `docker-compose up`

If you want to run the local version of the cloned repository, you can change the following line in the docker-compose.yml

```yaml
command: [ "", "" ]
```

to 

```yaml
command: ["local", "/tmp/blockchain-messenger"]
```

## API

### status

#### GET

Returns the status of the blockchain.

`GET {{node_specific_url}}/status`

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

### messages

#### GET
Returns the pending messages to be mined.

`GET {{node_specific_url}}/messages`

<details>
<summary> Example Response </summary>

```json
[
  {
    "beneficiary": "node2",
    "id": "1zJmN4",
    "message": "cLQXUe5qtBjdcOWgXmic0YKNQx8w3UjWj3zGEIiEOORtSXgsaKACbNN4GgUCaPOT0tsiK0omzGCmf2i+UJFQnhU15An6qUZtqiWZSHD/HjOuWCSskiXq6OQ/5qo3McwZR88e3YUF5Am4E9GVhTOHfzV1js3cqDdcigR8uy3MzD0=",
    "originator": "node1",
    "value": 1
  }
]
```

</details>

#### POST

Add message to pending messages to be mined.

`POST {{node_specific_url}}/messages`

Example Response: 
```json
OK
```
OR
```json
Node {node_id} has a balance of {X} which is insufficient to schedule this message, costing {Y}
```

### mine

#### GET

Mines pending messages and adds them in a new block to the distributed blockchain.

The node that completes this action is rewarded with tokens.

`GET {{node_specific_url}}/mine`

Example Response: 

```json
OK
```

### my-messages

#### GET

Collect any messages on the blockchain that are addressed to the host node of this command. Decrypt and display the messages.

`GET {{node_specific_url}}/my-messages`

<details>
<summary> Example Response </summary>

```json
[
  {
    "beneficiary": "node2",
    "id": "jOkivz",
    "message": "hi there node2",
    "originator": "node1",
    "value": 1
  }
]
```

</details>

### balance

Returns the tokens owned by the node where this command is executed.

`GET {{node_specific_url}}/balance`

Example Response: 

`19`

# Credits

The first components of this project were written following Luca Florio's tutorial. His code is hosted in this repo: https://github.com/elleFlorio/scalachain