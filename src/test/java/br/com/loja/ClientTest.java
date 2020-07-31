package br.com.loja;

import br.com.loja.modelo.Carrinho;
import br.com.loja.modelo.Produto;
import com.thoughtworks.xstream.XStream;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ClientTest {

    private HttpServer server;
    private WebTarget target;
    private Client client;

    @Before
    public void startServer(){
       server = Server.initServer();

        ClientConfig config = new ClientConfig();
        config.register(new LoggingFilter());

       //Criando uma Client HTTP
       this.client = ClientBuilder.newClient();
       //Setando o meu alvo de busca (Dominio)
        this.target = client.target("http://localhost:8080");
    }

    @After
    public void finishServer(){
        server.stop();
    }

    @Test
    public void testaQueAConexaoComOSevidorFunciona(){
        //Criando uma Client HTTP
        Client client = ClientBuilder.newClient();
        //Setando o meu alvo de busca (Dominio)
        WebTarget target = client.target("http://www.mocky.io");
        //Aqui estou esperando uma string da requisição
        String conteudo = target.path("/v2/52aaf5deee7ba8c70329fb7d").request().get(String.class);
        Assert.assertTrue(conteudo.contains("Rua Vergueiro 3185"));
    }

    @Test
    public void testaQueBuscarUmCarrinhoTrazCarrinhoEsperado(){
        //Criando uma Client HTTP
        //Client client = ClientBuilder.newClient();
        //Setando o meu alvo de busca (Dominio)
        //WebTarget target = client.target("http://localhost:8080");
        //Aqui estou esperando uma string da requisição
        String conteudo = target.path("/carrinhos/1").request().get(String.class);
        Carrinho carrinho = (Carrinho) new XStream().fromXML(conteudo);
        Assert.assertEquals("Rua Vergueiro 3185, 8 andar", carrinho.getRua());
    }

    @Test
    public void testaCriacaoDeCarrinho(){
        Carrinho carrinho = new Carrinho();
        carrinho.adiciona(new Produto(314L, "Tablet", 999, 1));
        carrinho.setRua("Rua Vergueiro");
        carrinho.setCidade("Sao Paulo");
        String xml = carrinho.toXML();

        Entity<String> entity = Entity.entity(xml, MediaType.APPLICATION_XML);

        //Envio o post criando o /carrinhos
        Response response = target.path("/carrinhos").request().post(entity);

        Assert.assertEquals(201, response.getStatus());

        //Busca o valor do header Location
        String location = response.getHeaderString("Location");

        String conteudo = client.target(location).request().get(String.class);
        Assert.assertTrue(conteudo.contains("Tablet"));
    }

}
