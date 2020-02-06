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

def taskRegistration = { factory, prefix ->
	return { term -> bot.taskManager().register(prefix + " - " + term, factory.create(term)) }
}

def immowelt = taskRegistration(bootstrap.immoweltScraperTask(), 'Immowelt')
def ebay = taskRegistration(bootstrap.ebayScraperTask(), 'Ebay')

immowelt("91413 Neustadt an der Aisch")
immowelt("97346 Iphofen")
ebay("91413 Neustadt an der Aisch")
ebay("97346 Iphofen")


def process = 'geckodriver'.execute()
process.consumeProcessOutput(System.out, System.err)

while (true) {
	bot.pollUpdates(Duration.ofSeconds(30))
}
