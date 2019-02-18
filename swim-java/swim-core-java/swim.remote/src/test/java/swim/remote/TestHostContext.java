// Copyright 2015-2019 SWIM.AI inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package swim.remote;

import swim.api.auth.Credentials;
import swim.api.auth.Identity;
import swim.api.data.DataFactory;
import swim.api.downlink.Downlink;
import swim.api.policy.Policy;
import swim.api.policy.PolicyDirective;
import swim.concurrent.Schedule;
import swim.concurrent.Stage;
import swim.math.Z2Form;
import swim.runtime.HostContext;
import swim.runtime.HttpBinding;
import swim.runtime.LaneBinding;
import swim.runtime.LinkBinding;
import swim.runtime.NodeBinding;
import swim.runtime.PushRequest;
import swim.store.ListDataBinding;
import swim.store.MapDataBinding;
import swim.store.SpatialDataBinding;
import swim.store.ValueDataBinding;
import swim.structure.Value;
import swim.uri.Uri;

public class TestHostContext extends TestCellContext implements HostContext {
  protected final Uri hostUri;

  public TestHostContext(Uri hostUri, Policy policy, Schedule schedule, Stage stage, DataFactory data) {
    super(policy, schedule, stage, data);
    this.hostUri = hostUri;
  }

  public TestHostContext(Uri hostUri, Stage stage) {
    this(hostUri, null, stage, stage, null);
  }

  public TestHostContext(Uri hostUri) {
    this(hostUri, null, null, null, null);
  }

  @Override
  public Uri meshUri() {
    return Uri.empty();
  }

  @Override
  public Value partKey() {
    return Value.absent();
  }

  @Override
  public Uri hostUri() {
    return hostUri;
  }

  @Override
  public NodeBinding createNode(Uri nodeUri) {
    return null;
  }

  @Override
  public NodeBinding injectNode(Uri nodeUri, NodeBinding node) {
    return node;
  }

  @Override
  public LaneBinding injectLane(Uri nodeUri, Uri laneUri, LaneBinding lane) {
    return lane;
  }

  @Override
  public ListDataBinding openListData(Value name) {
    return null;
  }

  @Override
  public ListDataBinding injectListData(ListDataBinding dataBinding) {
    return dataBinding;
  }

  @Override
  public MapDataBinding openMapData(Value name) {
    return null;
  }

  @Override
  public MapDataBinding injectMapData(MapDataBinding dataBinding) {
    return dataBinding;
  }

  @Override
  public <S> SpatialDataBinding<S> openSpatialData(Value name, Z2Form<S> shapeForm) {
    return null;
  }

  @Override
  public <S> SpatialDataBinding<S> injectSpatialData(SpatialDataBinding<S> dataBinding) {
    return dataBinding;
  }

  @Override
  public ValueDataBinding openValueData(Value name) {
    return null;
  }

  @Override
  public ValueDataBinding injectValueData(ValueDataBinding dataBinding) {
    return dataBinding;
  }

  @Override
  public LinkBinding bindDownlink(Downlink downlink) {
    return null;
  }

  @Override
  public void openDownlink(LinkBinding link) {
  }

  @Override
  public void closeDownlink(LinkBinding link) {
  }

  @Override
  public void httpDownlink(HttpBinding http) {
  }

  @Override
  public void pushDown(PushRequest pushRequest) {
  }

  @Override
  public PolicyDirective<Identity> authenticate(Credentials credentials) {
    return null;
  }

  @Override
  public void didConnect() {
  }

  @Override
  public void didDisconnect() {
  }

  @Override
  public void close() {
  }

  @Override
  public void willOpen() {
  }

  @Override
  public void didOpen() {
  }

  @Override
  public void willLoad() {
  }

  @Override
  public void didLoad() {
  }

  @Override
  public void willStart() {
  }

  @Override
  public void didStart() {
  }

  @Override
  public void willStop() {
  }

  @Override
  public void didStop() {
  }

  @Override
  public void willUnload() {
  }

  @Override
  public void didUnload() {
  }

  @Override
  public void willClose() {
  }
}
