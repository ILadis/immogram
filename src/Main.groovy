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

def immoscout = manager.register('Immoscout', bootstrap.immoscoutScraperTask())
def immowelt = manager.register('Immowelt', bootstrap.immoweltScraperTask())
def ebay = manager.register('Ebay', bootstrap.ebayScraperTask())

def period = Duration.ofHours(3)
def terms = props['SEARCH_TERMS'].tokenize(':')

terms.each { term ->
	immoscout.create(term).schedule(period)
	immowelt.create(term).schedule(period)
	ebay.create(term).schedule(period)
}

def server = bootstrap.feedServer()
server.setAdminPassword(props['ADMIN_PASSWD'])
