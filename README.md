# About TwitterTrader

TwitterTrader is an experimental project in linking crypto exchanges to the twitter api, which 
allows the use of twitter data as a trade indicator. 


# How to use it?

To use the program, the `Bot` class needs an `Exchange`, `Indicator`, and double representing the 
amount of coin you're willing to trade in each use of the bot. 

Once the program is run, it will attempt to trade once and then exit. This can take some time 
depending on the state of the market and how quickly it can detect the indicator through twitter.

First you will need to set up rules for twitter search. These should be created using 
[twitter guidlines](https://developer.twitter.com/en/docs/twitter-api/tweets/filtered-stream/integrate/build-a-rule#build).

The following code gives an example:
```
String ruleBody = "(cat) (cute OR tiny OR funny) (-nasty -dog -mean)";
String ruleTag = "good cats";
HashMap<String, String> rules = new HashMap<>();
rules.put(ruleBody, ruleTag);
```

Then once your rules are setup, you can then create and run the bot using the following code:

```
Indicator explorer = new TwitterRestExplorer(rules);
Exchange exchange = new XchangeExchange(CurrencyPair.BTC_USD, true);
double tradeAmount = .001;

try (Bot bot = new Bot(tradeAmount, explorer, exchange)) {
  bot.init();
  while (bot.isRunning())
    Thread.sleep(100);
  }
}
```



# Can I use this to trade real money?

Theoretically yes, however I would recommend significant changes to the code. As it stands right 
now, the bot is not designed to make money, and therefore trading real money is entirely luck based.
Creating a new class that implements `Indicator` and changing sell logic may make the bot better able
to fulfill this purpose. 

To swap it from paper trading to real trading, you'll need a second set of keys to login, and to 
disable sandbox mode you can simply set the second parameter of the XchangeExchange to false.