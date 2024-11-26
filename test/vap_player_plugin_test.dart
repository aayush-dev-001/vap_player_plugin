import 'package:flutter_test/flutter_test.dart';
import 'package:vap_player_plugin/vap_player_plugin.dart';
import 'package:vap_player_plugin/vap_player_plugin_platform_interface.dart';
import 'package:vap_player_plugin/vap_player_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockVapPlayerPluginPlatform
    with MockPlatformInterfaceMixin
    implements VapPlayerPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final VapPlayerPluginPlatform initialPlatform = VapPlayerPluginPlatform.instance;

  test('$MethodChannelVapPlayerPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelVapPlayerPlugin>());
  });

  test('getPlatformVersion', () async {
    VapPlayerPlugin vapPlayerPlugin = VapPlayerPlugin();
    MockVapPlayerPluginPlatform fakePlatform = MockVapPlayerPluginPlatform();
    VapPlayerPluginPlatform.instance = fakePlatform;

    expect(await vapPlayerPlugin.getPlatformVersion(), '42');
  });
}
