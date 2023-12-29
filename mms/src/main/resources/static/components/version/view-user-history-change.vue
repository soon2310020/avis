<template>
  <div
    id="view-history-change"
    class="modal fade"
    tabindex="-1"
    role="dialog"
    aria-labelledby="exampleModalLongTitle"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" v-text="resources['user']"></h5>
          <button type="button" class="close" @click.prevent="close()"><span aria-hidden="true">&times;</span></button>
        </div>
        <div class="modal-body" v-if="!loading">
          <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header">
                            <strong v-text="resources['account']"></strong>
                        </div>
                        <div class="card-body">
                            <div class="form-group row">
                                <span class="col-md-2 col-form-label" for="name" v-text="resources['name']"></span>
                                <div class="col-md-10">
                                    <!-- <input type="email" id="name" class="form-control" v-model="userAfter.name" value="123" readonly> -->
                                    <span type="text" id="name" class="form-control">
                                      <span v-bind:class="{'version-change-old': userAfter.name != userBefore.name}">{{userBefore.name}}</span>
                                      <span v-if="userAfter.name !== userBefore.name" class="version-change-new">{{userAfter.name}}</span>
                                      <span style="visibility: hidden">.</span>
                                    </span>
                                </div>
                            </div>

                            <div class="form-group row">
                                <span class="col-md-2 col-form-label" for="email" v-text="resources['email']"></span>
                                <div class="col-md-10">
                                    <!-- <input type="email" id="email" class="form-control" v-model="userAfter.email" value="123" readonly> -->
                                    <span type="text" id="email" class="form-control">
                                      <span v-bind:class="{'version-change-old': userAfter.email != userBefore.email}">{{userBefore.email}}</span>
                                      <span v-if="userAfter.email !== userBefore.email" class="version-change-new">{{userAfter.email}}</span>

                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

              <div class="row">
                <div class="col-lg-12">
                    <div class="card">
                        <div class="card-header">
                            <strong v-text="resources['profile']"></strong>
                        </div>
                        <div class="card-body">
                            <div class="form-group row">
                                <span class="col-md-2 col-form-label" for="name"> <span v-text="resources['company']"></span><span
                                        class="avatar-status badge-danger"></span></span>
                                <div class="col-md-10">
                                    <span type="text" id="name" class="form-control">
                                      <span v-bind:class="{'version-change-old': userAfter.companyName != userBefore.companyName}">{{userBefore.companyName}}</span>
                                      <span v-if="userAfter.companyName !== userBefore.companyName" class="version-change-new">{{userAfter.companyName}}</span>
                                      <span style="visibility: hidden">.</span>
                                    </span>

                                </div>
                            </div>

                            <div class="form-group row">
                                <span class="col-md-2 col-form-label" for="text-input1" v-text="resources['department']"></span>
                                <div class="col-md-4">
                                    <span class="form-control" id="text-input1" placeholder="Department">
                                      <span v-bind:class="{'version-change-old': userAfter.department != userBefore.department}">{{userBefore.department}}</span>
                                      <span v-if="userAfter.department !== userBefore.department" class="version-change-new">{{userAfter.department}} </span>
                                      <span style="visibility: hidden">.</span>
                                    </span>
                                </div>
                                <span class="col-md-2 col-form-label" for="text-input2" v-text="resources['position']"></span>
                                <div class="col-md-4">
                                    <span class="form-control" id="text-input2">
                                      <span v-bind:class="{'version-change-old': userAfter.position != userBefore.position}">{{userBefore.position}}</span>
                                      <span v-if="userAfter.position !== userBefore.position" class="version-change-new">{{userAfter.position}}</span>
                                      <span style="visibility: hidden">.</span>
                                    </span>
                                </div>
                            </div>

                            <div class="form-group row">
                                <span class="col-md-2 col-form-label" for="contactNumber">
                                 <span v-text="resources['contact_number']"></span> <span
                                        class="avatar-status badge-danger"></span></span>
                                <div class="col-md-4">
                                    <span type="text" class="form-control" id="contactNumber">
                                      <span v-bind:class="{'version-change-old': userAfter.contactNumber != userBefore.contactNumber}">{{userBefore.contactNumber}}</span>
                                      <span v-if="userAfter.contactNumber !== userBefore.contactNumber" class="version-change-new">{{userAfter.contactNumber}}</span>
                                      <span style="visibility: hidden">.</span>
                                    </span>
                                </div>
                                <span class="col-md-2 col-form-label" for="mobileNumber">
                                <span v-text="resources['mobile_number']"></span><span
                                        class="avatar-status badge-danger"></span></span>
                                <div class="col-md-4">
                                      <span type="text" class="form-control" id="mobileNumber">
                                        <span v-bind:class="{'version-change-old': userAfter.mobileNumber != userBefore.mobileNumber}">{{userBefore.mobileNumber}}</span>
                                        <span v-if="userAfter.mobileNumber !== userBefore.mobileNumber" class="version-change-new">{{userAfter.mobileNumber}}</span>
                                        <span style="visibility: hidden">.</span>
                                    </span>
                                </div>
                            </div>

                            <div class="form-group row">
                                <span class="col-md-2 col-form-label" for="memo">  <span v-text="resources['memo']"></span><span
                                        class="avatar-status badge-danger"></span></span>
                                <div class="col-md-10">
                                    <!-- <input type="text" id="memo" v-model="userAfter.memo" readonly="readonly"
                                        class="form-control" required> -->
                                      <span type="text" class="form-control" id="memo">
                                        <span v-bind:class="{'version-change-old': userAfter.memo != userBefore.memo}">{{userBefore.memo}}</span>
                                        <span v-if="userAfter.memo !== userBefore.memo" class="version-change-new">{{userAfter.memo}}</span>
                                        <span style="visibility: hidden">.</span>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
              </div>
              <div class="row">
                  <div class="col-lg-12">
                    <div class="card">
                      <div class="card-header">
                        <strong v-text="resources['access']"></strong>
                      </div>
                      <div class="card-body">
                        <div class="form-group row">
                          <span class="col-md-2 col-form-label" for="enable">
                          <span v-text="resources['enable']"></span>
                            <span class="avatar-status badge-danger"></span>
                          </span>
                          <div class="col-md-4">
                            <!-- <input type="text" id="enable" v-model="userAfter.enabled" class="form-control" readonly> -->
                            <span type="text" class="form-control" id="enable">
                                        <span v-bind:class="{'version-change-old': userAfter.enabled != userBefore.enabled}">{{userBefore.enabled}}</span>
                                        <span v-if="userAfter.enabled !== userBefore.enabled" class="version-change-new">{{userAfter.enabled}}</span>
                                        <span style="visibility: hidden">.</span>
                            </span>
                          </div>

                          <span class="col-md-2 col-form-label" for="accessLevel">
                          <span v-text="resources['access_level']"></span>
                            <span class="avatar-status badge-danger"></span>
                          </span>
                          <div class="col-md-4">
                            <!-- <input type="text" id="accessLevel" class="form-control" v-model="userAfter.accessLevel" readonly> -->
                            <span type="text" class="form-control" id="accessLevel">
                              <span v-bind:class="{'version-change-old': userAfter.accessLevel != userBefore.accessLevel}">{{userBefore.accessLevel}}</span>
                              <span v-if="userAfter.accessLevel !== userBefore.accessLevel" class="version-change-new">{{userAfter.accessLevel}}</span>
                              <span style="visibility: hidden">.</span>
                            </span>
                          </div>
                        </div>

                        <div class="form-group row">
                          <span class="col-md-2 col-form-label" for="accessFeature">
                          <span v-text="resources['access_feature']"></span>
                            <span class="avatar-status badge-danger"></span>
                          </span>
                          <div class='col-md-4'>
                            <div v-for="(role, authority) in roles" v-if="role.roleType == 'ROLE_MENU'" :key='authority'>
                              <div v-if="role.compare === 2">
                                <input type="checkbox" class="input_checkbox" checked disabled/>{{role.name}} ({{role.description}})
                              </div>
                              <label class="container" v-else-if="role.compare === -1">
                                <span>{{role.name}} ({{role.description}})</span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="role.compare === 1">
                                <span>{{role.name}} ({{role.description}})</span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                              <div v-if="role.compare === -2">
                                <input type="checkbox" class="input_checkbox" disabled/>{{role.name}} ({{role.description}})
                              </div>
                            </div>
                          </div>
                          <span class="col-md-2 col-form-label" for="accessGroup">
                          <span v-text="resources['access_group']"></span>
                            <span class="avatar-status badge-danger"></span>
                          </span>
                          <div class='col-md-4'>
                            <div v-for="(role, authority) in roles" v-if="role.roleType == 'ROLE_GROUP'" :key='authority'>
                              <div v-if="role.compare === 2">
                                <input type="checkbox" class="input_checkbox" checked disabled/>{{role.name}} ({{role.description}})
                              </div>
                              <label class="container" v-else-if="role.compare === -1">
                                <span>{{role.name}} ({{role.description}})</span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom"></span>
                              </label>
                              <label class="container" v-else-if="role.compare === 1">
                                <span>{{role.name}} ({{role.description}})</span>
                                <input type="checkbox" checked="checked" disabled>
                                <span class="checkmark-custom-2"></span>
                              </label>
                              <div v-if="role.compare === -2">
                                <input type="checkbox" class="input_checkbox" disabled/>{{role.name}} ({{role.description}})
                              </div>
                            </div>
                          </div>
                        </div>

                      </div>
                    </div>
                  </div>
              </div>
        </div>
        <div class='modal-footer card-footer-close-btn'>
          <button type="button" @click.prevent='close()' class="btn btn-secondary"  v-text="resources['close']"></button>
        </div>
    </div>
  </div>
</template>

<script>
    const KEEP_CURRENT = 2;
    const REMOVE_CURRENT = 1;
    const NEW_CURRENT = -1;
    const REMOVE_LAST = -2;

module.exports = {
props: {
        resources: Object,
    },
  data() {
    return {
      param: Object,
      userAfter: {
        name: ''
      },
      userBefore: Object,
      loading: true,
      neAccessGroup: false,
      neAccessFeature: false,
        accessFeatures: [],
        roles: Object
    };
  },
  methods: {
    showView: function(revision) {
      let sef = this;
      sef.userAfter = null;
      sef.userBefore = null;
      sef.neAccessGroup = false;
      sef.neAccessFeature =false;
      axios.get('/api/version/revision-history/top2?id=' + revision.id+'&revisionObjectType='+ revision.revisionObjectType+'&originId='+ revision.originId).then(function(result){
          console.log(result);
          sef.userAfter = result.data.after;
          sef.userBefore = result.data.before;
          sef.getRoles();
          // if(sef.userAfter != null && sef.userBefore){
            sef.neAccessFeature = !sef.compareAccess(sef.userAfter.accessFeature, sef.userBefore.accessFeature);
            sef.neAccessGroup = !sef.compareAccess(sef.userAfter.accessGroup, sef.userBefore.accessGroup);
          // }
          sef.loading = false;
          if (sef.userAfter.accessFeature != null && sef.userAfter.accessFeature.length > 0) {
              sef.accessFeatures=sef.userAfter.accessFeature.map((a) =>{
                  return {
                      compare: KEEP_CURRENT,
                      name:a
                  };
              });

              sef.accessFeatures.forEach(a => {
                  if (sef.userBefore.accessFeature!=null && !sef.userBefore.accessFeature.includes(a.name)) {
                      a.compare = NEW_CURRENT
                  }
              });

          }
          if (sef.userBefore.accessFeature != null && sef.userBefore.accessFeature.length > 0) {
              sef.userBefore.accessFeature.forEach(access=> {
                  if (sef.userAfter.accessFeature!=null && !sef.userAfter.accessFeature.includes(access)) {
                      sef.accessFeatures.push({
                          compare: REMOVE_CURRENT,
                          name: access
                      })
                  }
              });
          }
          console.log("sef.userBefore.accessFeature: ", sef.userBefore.accessFeature);
          console.log("sef.userAfter.accessFeature: ", sef.userAfter.accessFeature);
          console.log("sef.accessFeatures: ", sef.accessFeatures);
      });
      $("#view-history-change").modal("show");
    },
    getRoles: function(){
        // get list role and compare role after and role before
        let sef= this;
        axios.get("/api/roles?size=99999").then(function(result){
            allRoles = result.data.content.map(value => value.role);
            console.log('Roles: ' + allRoles);
            if(allRoles != null && allRoles instanceof Array){
                let roleServer = allRoles.filter(role => role.roleType == 'ROLE_MENU' || role.roleType == 'ROLE_GROUP');
                let roleAfter = sef.userAfter.accessFeature.concat(sef.userAfter.accessGroup);
                let roleBefore = sef.userBefore.accessFeature.concat(sef.userBefore.accessGroup);
                let comapareBA = new Array();
                for(let countSum = 0; countSum < roleServer.length; countSum ++){

                    let compareAfterVsServer = false;
                    let compareBeforeVsServer = false;

                    for(let countAfter = 0; countAfter < roleAfter.length; countAfter ++){
                        console.log('Role after: ' + roleAfter[countAfter]);
                        console.log('Role server: ' + roleServer[countSum].authority);
                        if(roleAfter[countAfter] == roleServer[countSum].authority){
                            compareAfterVsServer = true;
                            break;
                        }
                    }

                    for(let countBefore = 0; countBefore < roleBefore.length; countBefore ++){
                        if(roleBefore[countBefore] == roleServer[countSum].authority){
                            compareBeforeVsServer = true;
                            break;
                        }
                    }

                    let compareAfterBefore = 0;
                    if(!compareAfterVsServer && compareBeforeVsServer) {
                        compareAfterBefore = 1;
                    }else if(compareAfterVsServer && !compareBeforeVsServer){
                        compareAfterBefore = -1;
                    }else if(compareAfterVsServer && compareBeforeVsServer){
                        compareAfterBefore = 2;
                    }else if(!compareAfterVsServer && !compareBeforeVsServer){
                        compareAfterBefore = -2;
                    }
                    comapareBA.push({
                        authority: roleServer[countSum].authority,
                        name: roleServer[countSum].name,
                        description: roleServer[countSum].description,
                        compare: compareAfterBefore,
                        roleType: roleServer[countSum].roleType
                    })
                }
                sef.roles = comapareBA;
                sef.roles.forEach(x => console.log(x));
            }
        });
    },
    compareAccess: function(accessOld, accessNew){
      try{
        if(accessOld.length != accessNew.length) return false;
        let check = false;
        for(let accessNewObj in accessNew){
            for(let old in accessOld){
              if(accessNewObj.equals(old)){
                check = true;
                break;
              }
            }
            if(!check){
              return false;
            }
        }
        return true;
      }catch(e){
        return true;
      }
    }
    ,
    close: function() {
      this.loading = true;
      $("#view-history-change").modal("hide");
      this.$emit('open-modal-custom');
    }
  }
};
</script>
<style scoped>
    .container {
        display: block;
        position: relative;
        margin: 0;
    }

    .container input {
        position: absolute;
        opacity: 0;
        cursor: pointer;
        height: 0;
        width: 0;
    }

    .checkmark-custom {
        position: absolute;
        top: 0;
        left: 0;
        height: 13px;
        width: 13px;
        background-color: #eee;
        border: 1px solid lightgray;
        border-radius: 3px;
    }

    .checkmark-custom-2 {
        position: absolute;
        top: 0;
        left: 0;
        height: 13px;
        width: 13px;
        background-color: #eee;
        border: 1px solid lightgray;
        border-radius: 3px;
    }
    .checkmark-custom:after {
        content: "";
        position: absolute;
        display: none;
    }

    .checkmark-custom-2:after {
        content: "";
        position: absolute;
        display: none;
    }

    .container input:checked ~ .checkmark-custom:after {
        display: block;
    }

    .container input:checked ~ .checkmark-custom-2:after {
        display: block;
    }

    .container .checkmark-custom:after {
        left: 3px;
        top: 1px;
        width: 5px;
        height: 8px;
        border: solid red;
        border-width: 0 3px 3px 0;
        -webkit-transform: rotate(45deg);
        -ms-transform: rotate(45deg);
        transform: rotate(45deg);
    }

    .container .checkmark-custom-2:after {
        left: 2px;
        top: 4px;
        width: 5px;
        height: 3px;
        border: solid red;
        border-width: 0 7px 0px 0;
    }

    #view-history-change {
        pointer-events: auto;
    }

</style>
