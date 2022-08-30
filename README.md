# ChatCoPlus

[![discord](https://img.shields.io/discord/843551077759844362?logo=discord)](https://discord.gg/7tW8ZAtGr5)
[![reddit](https://img.shields.io/reddit/subreddit-subscribers/0b0t)](https://old.reddit.com/r/0b0t/)
[![release](https://github.com/zeroBzeroT/ChatCoPlus/actions/workflows/release.yml/badge.svg)](https://github.com/zeroBzeroT/ChatCoPlus/actions/workflows/release.yml)
![repo size](https://img.shields.io/github/languages/code-size/zeroBzeroT/ChatCoPlus.svg?label=repo%20size)
[![downloads](https://img.shields.io/github/downloads/zeroBzeroT/ChatCoPlus/total)](https://github.com/zeroBzeroT/ChatCoPlus/releases)

---

ChatCoPlus is a rebuild of the famous chat plugin ChatCo created by [jj20051](https://github.com/WiredTombstone), who is
the founder and current owner of 9b9t.

### Features

- Players can customize their chat message using various prefixes. As an example putting an '>' before a chat message
  will turn the text green (green-text). (Note: Does not change the color of player names).
- Players can whisper other players (defaults to a purple color).
- Players can ignore another player (works for both world and whisper chat). Ignore lists are persistent between logins.
  Players can also un-ignore a previously ignored player.
- Allows for a customizable spoiler text system (who wants to ruin the walking dead for everyone?).
- Players can toggle chat on/off.
- Full permission support so that you can limit who has access to what colors / features.
- Allows the administrator to log whispers between players. (This might be against local law.)

### Popular servers running this plugin

- 0b0t.org
- 2b2t.net
- 9b9t.com
- Anarchy.pw

### Administrative commands

Every single admin command goes by the format "/chatco {component} {e|d}" where 'e' enables and 'd' disables, e.g.
typing "/chatco
whisperlog e" in the console will enable whisper logging. You will have to reload the plugin in order to make most of
the
changes take effect.

### Components

- **spoilers** - enables or disables spoilers, is **disabled** by default.
- **whispers** - enables or disables whisper changes, is **enabled** by default.
- **newcommands** - enables or disables new whisper commands, is **enabled** by default.
- **whisperlog** - enables or disables whisper logging (whisper logs are saved in /ChatCo/whisperlog.txt), is **
  disabled**
  by default.)

### Player Commands

- **/ignore {player}** - ignores or un-ignores the player.
- **/ignorelist** - prints all ignored players.
- **/unignoreall** - clears ignore list
- **/togglechat** - disables regular chatting for the player - NOT PERSISTENT.
- **/toggletells** - disables tells for the player - NOT PERSISTENT,

### Whisper Related Player Commands

- **/r** and **/reply** - replies to the last person who sent you a whisper this session.
- **/l** and **/last** - replies to the last person you sent a whisper to

### Config

Color codes and prefixes can be disabled by replacing the contents with NULL e.g. GREEN: NULL.
Usage of color codes and prefixes can be restricted by using permissions.
You can customize the appearance of whispers.

### Examples

- Turn chat off/on with /togglechat.
- Write > at the start of a message for green-text.
- Write [SPOILER]text[/SPOILER] to make a spoiler.
- Write /show spoiler [1-5] to view spoilers.

### Planned Features

- Fully Async Chat

### Tested Minecraft Versions

- 1.12.2

### Git Release Creation

```bash
git tag <tag_name>
git push origin <tag_name>
```