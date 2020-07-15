package Groovy_Scripts

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

def fileName = "./test_data/draft.xml"
def xmlFile = getClass().getResourceAsStream(fileName)

Node ovalParts = new XmlParser().parse(xmlFile)
def total = 0
def changelist =[]

//Parts to remove
def testNode = ovalParts.tests
def objectNode = ovalParts.objects
def stateNode = ovalParts.states
def variableNode = ovalParts.variables

ovalParts.remove(testNode)
ovalParts.remove(objectNode)
ovalParts.remove(stateNode)
ovalParts.remove(variableNode)

ovalParts.definitions.definition.each {
    def it -> {
        Node statusNode = it.metadata.oval_repository.status[0] as Node
        NodeList statusChanges = it.metadata.oval_repository.dates.status_change
        NodeList dates = it.metadata.oval_repository.dates

        println statusChanges

        def status = statusNode.text().replaceAll("\\s","")

        ZonedDateTime from = ZonedDateTime.now()

        if (statusChanges.last() != null) {
            from = ZonedDateTime.parse((String)statusChanges.last().@date, DateTimeFormatter.ISO_DATE_TIME)
        }

        ZonedDateTime to = ZonedDateTime.now();

        long days = ChronoUnit.DAYS.between(from, to)

        boolean correctStatus = status == 'INTERIM'
        boolean olderThanTwoWeeks = days >= 13

        if (correctStatus && olderThanTwoWeeks) {

            changelist << "$it.metadata.title"
            total++

            def newStatusChange = new NodeBuilder().status_change(date: "${to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).toString()}", 'ACCEPTED')
//            println newStatusChange

            dates[0].children().add(dates.size() + 1, newStatusChange)
            statusNode.value = 'ACCEPTED'
        }
    }
}

new XmlNodePrinter(new PrintWriter(new FileWriter(fileName))).print(ovalParts)

changelist.each {println(it)}
println(total)
