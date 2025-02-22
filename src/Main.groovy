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
		.webDriverEndpoint('http://localhost:4444', props['GECKO_PROFILE'], true)
		.feedServer('0.0.0.0', 8080, 'http://localhost:8080/')
		.build()

def manager = bootstrap.taskManager()
def period = Duration.ofHours(3)

def immoscout = manager.register('Immoscout', bootstrap.immoscoutScraperTask())
def immowelt = manager.register('Immowelt', bootstrap.immoweltScraperTask())
def ebay = manager.register('Ebay', bootstrap.ebayScraperTask())

immoscout.create('90537 Feucht').schedule(period)
immowelt.create('90537 Feucht').schedule(period)
ebay.create('90537 Feucht').schedule(period)

def server = bootstrap.feedServer()
server.setAdminPassword(props['ADMIN_PASSWD'])
