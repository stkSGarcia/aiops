package io.transwarp.aiops.plain.dao

import java.net.InetAddress

import io.transwarp.aiops.plain.conf.EsConf
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient

object EsClient {
  val esClientThreadLocal = new ThreadLocal[TransportClient] {
    override def initialValue: TransportClient = {
      val settings = Settings.builder().put("cluster.name", EsConf.getConf.clusterName).build()
      new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName
      (EsConf.getConf.host), EsConf.getConf.port))
    }
  }

  def getThreadLocal: TransportClient = {
    esClientThreadLocal.get
  }
}
