package com.ecommerce.page.mq;

import com.ecommerce.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {
    @Autowired
    private PageService pageService;

    /*
     * Handling product additions and changes in static page
     * @param: spuId
     * @return void
     * @author dunklee
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.insertOrUpdate.queue", durable = "true"),
            exchange = @Exchange(name = "leyou.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void ListenInsertOrUpdate(Long spuId) {
        if (spuId == null) {
            return;
        }
        pageService.createHtml(spuId);
    }

    /*
     * Handling item deletion in static page
     * @param: spuId
     * @return void
     * @author dunklee
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.delete.queue", durable = "true"),
            exchange = @Exchange(name = "leyou.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void ListenDelete(Long spuId) {
        if (spuId == null) {
            return;
        }
        pageService.deleteHtml(spuId);
    }
}
