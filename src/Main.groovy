#!/usr/bin/env groovy
import java.time.*

def env = System.getenv()
def classLoader = this.class.classLoader.rootLoader

(env.getOrDefault('LIBS', './lib') as File).eachFile { file ->
	classLoader.addURL(file.toURL())
}

def props = new Properties()
(env.getOrDefault('PROPS', './props') as File).withInputStream { stream ->
	props.load(stream)
}

def gecko = 'geckodriver'.execute()
gecko.consumeProcessOutput(System.out, System.err)

def builder = {
	return Class.forName('immogram.Bootstrap')
			.getMethod('newBuilder')
			.invoke(null)
}

def bootstrap = builder()
		.messagesLocale(Locale.GERMAN)
		.jdbcUrl(props['JDBC_URL'])
		.webDriverEndpoint('http://localhost:4444')
		.telegramApiTokenAndEndpoint(props['BOT_TOKEN'], 'https://api.telegram.org')
		.build()

def bot = bootstrap.immogramBot()
def manager = bot.taskManager()

def immowelt = manager.register("Immowelt", bootstrap.immoweltScraperTask())
def ebay = manager.register("Ebay", bootstrap.ebayScraperTask())

immowelt.create("91413 Neustadt an der Aisch")
immowelt.create("97346 Iphofen")
ebay.create("91413 Neustadt an der Aisch")
ebay.create("97346 Iphofen")

def timeout = Duration.ofSeconds(30)

while (true) {
	bot.pollUpdates(timeout)
}
