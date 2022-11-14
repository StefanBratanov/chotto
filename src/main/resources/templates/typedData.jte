@import gg.jte.support.ForSupport
@import chotto.objects.BatchContribution

@param BatchContribution model

{
  "types": {
    "EIP712Domain": [
      {
        "name": "name",
        "type": "string"
      },
      {
        "name": "version",
        "type": "string"
      },
      {
        "name": "chainId",
        "type": "uint256"
      }
    ],
    "contributionPubkey": [
      {
        "name": "numG1Powers",
        "type": "uint256"
      },
      {
        "name": "numG2Powers",
        "type": "uint256"
      },
      {
        "name": "potPubkey",
        "type": "bytes"
      }
    ],
    "PoTPubkeys": [
      {
        "name": "potPubkeys",
        "type": "contributionPubkey[]"
      }
    ]
  },
  "primaryType": "PoTPubkeys",
  "domain": {
    "name": "Ethereum KZG Ceremony",
    "version": "1.0",
    "chainId": 1
  },
  "message": {
    "potPubkeys": [
      @for(var contribution : ForSupport.of(model.getContributions()))
        {
          "numG1Powers": ${contribution.get().getNumG1Powers()},
          "numG2Powers": ${contribution.get().getNumG2Powers()},
          "potPubkey": "${contribution.get().getPotPubkey().orElseThrow().toHexString()}"
        }
        @if(!contribution.isLast())
          ,  
        @endif  
      @endfor
    ]
  }
}