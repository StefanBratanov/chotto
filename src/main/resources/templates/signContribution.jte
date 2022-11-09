@import chotto.template.TemplateResolver.SignContributionHtmlModel

@param SignContributionHtmlModel model

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Sign Contribution</title>
    <script src="/static/web3.min.js"></script>
</head>

<body>
<div id="info"></div>
<script>

        function changeInfoTagContent(content) {
            document.getElementById("info").innerHTML = content
        }
        @raw
        function changeInfoTagContentWithError(error) {
            changeInfoTagContent(`Error occurred while signing: ${error instanceof Error ? error.toString() : JSON.stringify(error)}. You can try refreshing this page and signing again.`)
        }
        @endraw
        const typedData = "${model.typedData}"
        const ethAddress = "${model.ethAddress}"
        const callbackPath = "$unsafe{model.callbackPath}"

        async function signContributionSafely() {
            try {
                await signContribution()
            } catch (error) {
                changeInfoTagContentWithError(error)
            }
        }
        @raw
        async function signContribution() {
            const accounts = await web3.eth.requestAccounts();
            if (!accounts.some(account => account.toLowerCase() == ethAddress.toLowerCase())) {
                changeInfoTagContent(`Please log in into your wallet with ${ethAddress} instead of ${accounts} and then refresh this page.`)
                return;
            }
            const chainId = await web3.eth.getChainId();
            if (chainId != 1) {
                changeInfoTagContent(`Please log in into your wallet with mainnet (chainId: 1) instead of chainId: ${chainId} and then refresh this page.`)
                return;
            }
            web3.currentProvider.sendAsync(
                {
                    method: "eth_signTypedData_v4",
                    params: [ethAddress, typedData],
                    from: ethAddress
                },
                function (error, result) {
                    if (error) {
                        changeInfoTagContentWithError(error)
                        return
                    }
                    fetch(callbackPath + "?signature=" + result.result)
                        .then(response => response.text())
                        .then(text => changeInfoTagContent(text))
                        .catch(error => changeInfoTagContentWithError(error))
                }
            )
        }

        window.addEventListener('load', async () => {
            changeInfoTagContent(`Please log in into your wallet and follow the instructions to sign your contribution with your eth address ${ethAddress}.`)
            if (window.ethereum) {
                window.web3 = new Web3(ethereum);
                await signContributionSafely()
            }
            else if (window.web3) {
                window.web3 = new Web3(web3.currentProvider);
                await signContributionSafely()
            }
            else {
                changeInfoTagContent("Your browser does not support Ethereum. Please restart Chotto and disable ECDSA signing or install compatible browser wallet.")
            }
        });
        @endraw
</script>
</body>

</html>