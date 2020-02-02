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
		.jdbcUrl(props['JDBC_URL'])
		.webDriverEndpoint('http://localhost:4444')
		.telegramApiTokenAndEndpoint(props['BOT_TOKEN'], 'https://api.telegram.org')
		.build()


def immowelt = bootstrap.immoweltScraperTask()
def ebay = bootstrap.ebayScraperTask()

def tasks = [
		immowelt.create("91413 Neustadt an der Aisch"),
		immowelt.create("97346 Iphofen"),
		ebay.create("91413 Neustadt an der Aisch"),
		ebay.create("97346 Iphofen")]


def bot = bootstrap.immogramBot()

tasks.each { task ->
	bot.tasks().add(task)
	bot.tasks().setPeriod(Duration.ofHours(3))
}

def process = 'geckodriver'.execute()
process.consumeProcessOutput(System.out, System.err)

while (true) {
	bot.pollUpdates(Duration.ofSeconds(30))
}
