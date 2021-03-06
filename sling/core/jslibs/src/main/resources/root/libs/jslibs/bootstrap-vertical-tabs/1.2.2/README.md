Bootstrap Vertical Tabs
=======================

Vertical tabs component for Bootstrap 3.

#### Left Tabs
```html
<div class="col-xs-3"> <!-- required for floating -->
    <!-- Nav tabs -->
    <ul class="nav nav-tabs tabs-left">
      <li class="active"><a href="#home" data-toggle="tab">Home</a></li>
      <li><a href="#profile" data-toggle="tab">Profile</a></li>
      <li><a href="#messages" data-toggle="tab">Messages</a></li>
      <li><a href="#settings" data-toggle="tab">Settings</a></li>
    </ul>
</div>

<div class="col-xs-9">
    <!-- Tab panes -->
    <div class="tab-content">
      <div class="tab-pane active" id="home">Home Tab.</div>
      <div class="tab-pane" id="profile">Profile Tab.</div>
      <div class="tab-pane" id="messages">Messages Tab.</div>
      <div class="tab-pane" id="settings">Settings Tab.</div>
    </div>
</div>
```

#### Right Tabs
```html
<div class="col-xs-9">
  <!-- Tab panes -->
  <div class="tab-content">
    <div class="tab-pane active" id="home-r">Home Tab.</div>
    <div class="tab-pane" id="profile-r">Profile Tab.</div>
    <div class="tab-pane" id="messages-r">Messages Tab.</div>
    <div class="tab-pane" id="settings-r">Settings Tab.</div>
  </div>
</div>

<div class="col-xs-3"> <!-- required for floating -->
  <!-- Nav tabs -->
  <ul class="nav nav-tabs tabs-right">
    <li class="active"><a href="#home-r" data-toggle="tab">Home</a></li>
    <li><a href="#profile-r" data-toggle="tab">Profile</a></li>
    <li><a href="#messages-r" data-toggle="tab">Messages</a></li>
    <li><a href="#settings-r" data-toggle="tab">Settings</a></li>
  </ul>
</div>
```
####Sideways Tabs :new:

Add `sideways` class to tabs.

Example:
```
  <ul class="nav nav-tabs tabs-left sideways">
    ...
```

### License
[MIT](opensource.org/licenses/MIT)

### Author
Ismail Demirbilek, [@dbtek](http://twitter.com/dbtek).
