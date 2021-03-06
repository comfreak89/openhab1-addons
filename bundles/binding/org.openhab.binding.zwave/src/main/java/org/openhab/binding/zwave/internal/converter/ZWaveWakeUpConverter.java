/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

import java.util.Map;

import org.openhab.binding.zwave.internal.converter.command.BigDecimalCommandConverter;
import org.openhab.binding.zwave.internal.converter.command.IntegerCommandConverter;
import org.openhab.binding.zwave.internal.converter.command.ZWaveCommandConverter;
import org.openhab.binding.zwave.internal.converter.state.BigDecimalDecimalTypeConverter;
import org.openhab.binding.zwave.internal.converter.state.IntegerDecimalTypeConverter;
import org.openhab.binding.zwave.internal.converter.state.IntegerPercentTypeConverter;
import org.openhab.binding.zwave.internal.converter.state.ZWaveStateConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * ZWaveConfigurationConverter class. Converter for communication with the
 * {@link ZWaveConfigurationCommandClass}.
 *
 * @author Aitor Iturrioz
 * @since 1.9.0
 */
public class ZWaveWakeUpConverter extends ZWaveCommandClassConverter<ZWaveWakeUpCommandClass> {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveWakeUpConverter.class);
    private static final int REFRESH_INTERVAL = 0; // refresh interval in seconds for the binary switch;

    /**
     * Constructor. Creates a new instance of the {@link ZWaveWakeUpConverter} class.
     *
     * @param controller the {@link ZWaveController} to use for sending messages.
     * @param eventPublisher the {@link EventPublisher} to use to publish events.
     */
    public ZWaveWakeUpConverter(ZWaveController controller, EventPublisher eventPublisher) {
        super(controller, eventPublisher);

        // State and commmand converters used by this converter.
        this.addStateConverter(new IntegerDecimalTypeConverter());
        this.addStateConverter(new IntegerPercentTypeConverter());
        this.addCommandConverter(new BigDecimalCommandConverter());
        this.addStateConverter(new BigDecimalDecimalTypeConverter());
        this.addCommandConverter(new IntegerCommandConverter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerialMessage executeRefresh(ZWaveNode node, ZWaveWakeUpCommandClass commandClass, int endpointId,
            Map<String, String> arguments) {
        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), endpointId);
        return node.encapsulate(commandClass.getIntervalMessage(), commandClass, endpointId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(ZWaveCommandClassValueEvent event, Item item, Map<String, String> arguments) {
        ZWaveStateConverter<?, ?> converter = this.getStateConverter(item, event.getValue());
        if (converter == null) {
            logger.warn("No converter found for item = {}, node = {} endpoint = {}, ignoring event.", item.getName(),
                    event.getNodeId(), event.getEndpoint());
            return;
        }

        State state = converter.convertFromValueToState(event.getValue());
        this.getEventPublisher().postUpdate(item.getName(), state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receiveCommand(Item item, Command command, ZWaveNode node, ZWaveWakeUpCommandClass commandClass,
            int endpointId, Map<String, String> arguments) {
        ZWaveCommandConverter<?, ?> converter = this.getCommandConverter(command.getClass());
        if (converter == null) {
            logger.warn("NODE {}: No converter found for item={}, type={}, endpoint={}, ignoring command.",
                    node.getNodeId(), item.getName(), command.getClass().getSimpleName(), endpointId);
            return;
        }

        // Set the wakeup
        SerialMessage serialMessage = commandClass
                .setInterval((Integer) converter.convertFromCommandToValue(item, command));
        if (serialMessage == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass().getLabel(), endpointId);
            return;
        }

        this.getController().sendData(serialMessage);

        // And request a read-back
        serialMessage = commandClass.getIntervalMessage();
        this.getController().sendData(serialMessage);

        if (command instanceof State) {
            this.getEventPublisher().postUpdate(item.getName(), (State) command);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int getRefreshInterval() {
        return REFRESH_INTERVAL;
    }
}