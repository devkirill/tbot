package com.project.tbot.parser

import com.project.tbot.parser.model.Feed
import com.project.tbot.parser.model.Post
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.InputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


@Service
class XPathParser {
    private lateinit var builderFactory: DocumentBuilderFactory
    private lateinit var builder: DocumentBuilder

    init {
        try {
            builderFactory = DocumentBuilderFactory.newInstance()
            builder = builderFactory.newDocumentBuilder()
        } catch (e: ParserConfigurationException) {
            throw IllegalStateException(e)
        }
    }

    fun parseRss(inputStream: InputStream): Feed {
        val document = builder.parse(inputStream)

        return Feed {
            title = document.getValue("//channel/title")
            description = document.getValue("//channel/description")
            link = document.getValue("//channel/link")

            this.posts = document.getNodes("//channel/item")
                .map { node ->
                    Post {
                        guid = node.getValue("guid")
                        link = node.getValue("link")
                        title = node.getValue("title")
                        description = node.getValue("description")
                        images = node.getValues("images")
                        images += node.findNodes("enclosure")
                            .map { it as? Element }
                            .mapNotNull { it?.getAttribute("url") }
                        category = node.getValues("category")
                    }
                }
        }
    }

    fun Document.getNodes(expression: String): List<Node> = try {
        val xPath: XPath = XPathFactory.newInstance().newXPath()
        val nodeList: NodeList = xPath.compile(expression).evaluate(this, XPathConstants.NODESET) as NodeList

        val result = mutableListOf<Node>()
        for (i in 0 until nodeList.length) {
            result.add(nodeList.item(i))
        }

        result
    } catch (e: Exception) {
        throw IllegalStateException(e)
    }

    fun Node.getValue(path: String): String {
        return try {
            if (path.trim { it <= ' ' }.isEmpty()) return ""
            val xPath: XPath = XPathFactory.newInstance().newXPath()
            xPath.compile(path).evaluate(this, XPathConstants.STRING).toString().trim()
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    fun Node.getValues(path: String): List<String> = findNodes(path).map { it.textContent }

    fun Node.findNodes(path: String): List<Node> {
        try {
            if (path.trim { it <= ' ' }.isEmpty()) return listOf()

            val xPath: XPath = XPathFactory.newInstance().newXPath()

            val result = mutableListOf<Node>()
            val nodeList = xPath.compile(path).evaluate(this, XPathConstants.NODESET)

            if (nodeList is NodeList) {
                for (i in 0 until nodeList.length) {
                    val node = nodeList.item(i)
                    result.add(node)
                }
            }

            return result
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }
}