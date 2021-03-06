package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CopyNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class SDSCopyFeature implements Copy {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    private final PathContainerService containerService
        = new SDSPathContainerService();

    public SDSCopyFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final Node node = new NodesApi(session.getClient()).copyNodes(
                // Target Parent Node ID
                Long.parseLong(nodeid.getFileid(target.getParent(), new DisabledListProgressListener())),
                new CopyNodesRequest()
                    .addNodeIdsItem(Long.parseLong(nodeid.getFileid(source, new DisabledListProgressListener())))
                    .resolutionStrategy(CopyNodesRequest.ResolutionStrategyEnum.OVERWRITE), StringUtils.EMPTY, null);
            return new Path(target.getParent(), target.getName(), target.getType(),
                new SDSAttributesFinderFeature(session, nodeid).toAttributes(node));
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(containerService.isContainer(source)) {
            // Rooms cannot be copied
            return false;
        }
        if(Boolean.valueOf(containerService.getContainer(source).attributes().getCustom().get(SDSAttributesFinderFeature.KEY_ENCRYPTED))
            ^ Boolean.valueOf(containerService.getContainer(target).attributes().getCustom().get(SDSAttributesFinderFeature.KEY_ENCRYPTED))) {
            // If source xor target is encrypted data room we cannot use server side copy
            return false;
        }
        if(!StringUtils.equals(source.getName(), target.getName())) {
            // Cannot rename node to be copied at the same time
            return false;
        }
        if(Objects.equals(source.getParent(), target.getParent())) {
            // Nodes must not have the same parent
            return false;
        }
        return true;
    }
}
