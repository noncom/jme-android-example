## About

An example JMonkeyEngine3 project updated for Android Studio 4.

The project represents a template for a cross-platform desktop/Android app with
separate Gradle modules for each platform and shared code. Also some additional
modules are included, adding some additional features to your application. You
might want to remove unnecessary additional modules, code and resources since
they are included in this project to represent the most complete example. For example
it is easy to turn this into a desktop-only project.

*The general structure is based on ideas from https://habrahabr.ru/post/249305/*

Some additional works are included/referenced:
- JME-TTF by Adam T. Ryder -- this library is included in source form as a separate module for easier hacking (since TTF is not an easy). The modified version that this project uses is available at https://github.com/noncom/jME-TTF.git
- bits of my addon "Clarity" for Adams TTF library to enable basic TTF functions on the supported platforms
- LuaJ -- included in source form as a module and originally available at https://github.com/luaj/luaj

## Layout

The modules layout is the following:
- `app` -- android application module ready for adding your android-specific code. Your JME `AndroidHarness` implementation goes here. This module includes the android version of TTF support.
- `core` -- most of your cross-platform app code is supposed to be located here. This module is shared between `desktop` and `app`
- `desktop` -- desktop platform related stuff. Your JME `Application` implementation goes here.
- `jME-TTF` -- the copy of Adams code, slightly modified to fit the usage
- `luaj` -- the copy of LuaJ source code to enable easier hacking into Lua

## Usage

Don't forget `git submodule update --init` after you've cloned the repo.

Just add your code. You might want to remove unnecessary additional modules, code and resources since
they are included in this project to represent the most complete example.

If you plan on using the TTF support, do not forget to instantiate and initialize the corresponding `*Clarity` class.

This project is tested to work with (the default Android Studio 4.1.2 setup):
 - Android Studio 4.1.2
 - Gradle 6.5