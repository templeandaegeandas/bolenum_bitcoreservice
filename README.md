# Bitcoin Core Services

Bitcoin Core is a spring boot application build on java 1.8.
It provides core level api and has dependency for bitcoin core opearation like transction, wallet creation.
# Follow the below step to setup bitcoin core
<b>Step 1:</b><b> Java 1.8 Installation</b>
<br/>Download the <a href="http://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html">jdk-8u65-linux-x64.tar.gz</a> file.
<br/>Copy jdk-8u65-linux-x64.tar.gz  file to /opt/ folder and Extract it
<br/>Add these lines to .bashrc file
<br/><pre>JAVA_HOME=/opt/jdk-8u65-linux-x64/jdk1.8.0_64
<br/>export PATH=$PATH:$JAVA_HOME/bin</pre>
<br/>Hit this command to update the PATH variable:
<br/><pre>
<code>source ~/.bashrc</code></pre>

<b>Step 2:</b><b> Git installation</b>
<br/>To install Git, type the following command:
<br/><pre>
<code>sudo apt-get install git</code></pre>

<b>Step 3:</b><b> PostgreSql installation</b>
<br/>To install the server locally use the command line and type:
<pre><code>sudo apt-get install postgresql</code></pre>
To reset password to postgres user by root
<pre><code>sudo -u postgres psql postgres</code></pre>
After this run following command for password change and enter root as new password.
<pre><code>\password postgres</code></pre>
To create bitcoincore database for postgres user
<pre><code>create database bitcoincore;</code></pre>

<b>Step 4:</b><b> bitcoin d installation</b>
<br/>The following instructions describe installing Bitcoin Core on Linux systems.
If you use Ubuntu Desktop, click the Ubuntu swirl icon to start the Dash and type “term” into the input box.
Choose any one of the terminals listed:
Type the following line to add the Bitcoin Personal Package Archive (PPA) to your system:
<br/><pre>
<code> sudo apt-add-repository ppa:bitcoin/bitcoin</code></pre>
<br/>Type the following line to get the most recent list of packages:
<br/><pre>
<code> sudo apt-get update</code></pre>
<br/>To install both the GUI and the daemon, type the following line and read both the GUI instructions and the daemon instructions. 
Note that you can’t run both the GUI and the daemon at the same time using the same configuration directory.
<br/><pre>
<code>  sudo apt-get install bitcoin-qt bitcoind</code></pre>
<br/>or follow the link to download https://bitcoin.org/en/full-node#ubuntu-1410

<b>Step 5:</b><b> Maven Import</b>
<br/>Go to the git repository on your local system:
<br/><pre>
<code> cd git</code></pre>
Clone the bitcoin core repository to your git folder :
<br/><pre>
<code>git clone https://github.com/oodlestechnologies/bitcoin-core-services.git</code></pre>
<br/>Import the project as existing maven project in spring tool suite or another suitable IDE.

<b>Step 6:</b><b> Folder creation</b><br>
Navigate to <i> /opt </i> location and create a folder with name <i>secured</i> inside this folder create two folder named <i>wallet</i> and <i>walletqrcode</i> 

<b>Step 7:</b><b> Enabling security (OPTIONAL)</b><br>
If you are enabling security from <i>application.properties</i> file then you have to follow below step to successfully authenticate to bitcore core services:

1. Register your application with registeration API, You will get a Secret key and Api key that you need to save in your database.
2. During accessing bitcoin core APIs you need to set 3 headers : nonce, signature and API key .If you are using java then you can use the below code to generate signature using nonce and secret key:
<pre>
String nonce = String.valueOf(System.currentTimeMillis());
String secretKey = "YOUR_SECRET_KEY";
public static String generateHmacSHA256Signature(String nonce, String secretKey) throws GeneralSecurityException {

    byte[] hmacData;
    String encodedKey = null;

    try {
        SecretKeySpec secretkey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretkey);
        hmacData = mac.doFinal(nonce.getBytes("UTF-8"));
        encodedKey = Base64.getEncoder().encodeToString(hmacData);

    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    }
    return encodedKey;

}
</pre>

And for NodeJs:

<pre>    
var crypto = require('crypto');
var nonce = new Date().getTime();
var signature = function(nonce, secretKey) {
    var signature = crypto.createHmac('sha256', Buffer.from(secretKey, 'utf8')).update(Buffer.from(nonce, 'utf8')).digest('base64');
    return signature;
}
</pre>

3. You need to set 3 header with these keys to autenticate request:apikey,signature and nonce

# Running bitcoin core 

1. Start bitcoind using below command on terminal. <br>For running in local: <pre>sudo bitcoind -testnet -daemon</pre>
For running in production: <pre>sudo bitcoind -daemon</pre>

2. Get status of bitcoind using below command on terminal: <pre>sudo bitcoin-cli getinfo</pre>   

3. Check block height in this website : https://www.blocktrail.com and once the block height in both local and blocktrail.com matches then run bitcoin core project from your IDE.
4. It may take approx 15 min to synchronize bitcoin core with your local bitcoin block . 

# API Docs:

Run bitcoin core services and then open http://localhost:8080/api-docs/index.html in your browser. 
