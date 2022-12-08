@import gg.jte.support.ForSupport
@import chotto.objects.BatchTranscript
@import chotto.objects.SubContributionContext
@import java.util.List

@param BatchTranscript batchTranscript
@param List<SubContributionContext> subContributionContexts

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
      @for(var transcript : ForSupport.of(batchTranscript.getTranscripts()))
        {
          "numG1Powers": ${transcript.get().getNumG1Powers()},
          "numG2Powers": ${transcript.get().getNumG2Powers()},
          "potPubkey": "${subContributionContexts.get(transcript.getIndex()).getPotPubkey().toHexString()}"
        }
        @if(!transcript.isLast())
          ,
        @endif
      @endfor
    ]
  }
}