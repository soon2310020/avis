<template>
  <div tabindex="0" data-s-4="" class="access-group-container access-group-backdrop" id="hierarchy-container" style="overflow: auto;">
    <div id="legend" class="legend">
      <div class="legend-content">
        <div class="legend-item" v-for="(companyLevel, index) in companyLevels" :index="index"
             v-if="companyLevel.enabled">
          <div class="legend-icon"
               :style="{border: companyLevel.border ? companyLevel.border: '', backgroundColor: companyLevel.fillColor}"></div>
          <div class="legend-item">{{ companyLevel.name }}</div>
        </div>
      </div>
    </div>
    <svg id="hierarchy-content" v-if="drawData.length > 0"
      style="left: 0px; top: 0px; width: 100%; height: 100%; display: block; position: absolute; background-image: none; overflow: initial"
         :style="{left: offsetX + 'px'}"
    >
      <defs>
        <filter id="dropShadow">
          <feGaussianBlur in="SourceAlpha" stdDeviation="1.7" result="blur"></feGaussianBlur>
          <feOffset in="blur" dx="3" dy="3" result="offsetBlur"></feOffset>
          <feFlood flood-color="#3D4574" flood-opacity="0.4" result="offsetColor"></feFlood>
          <feComposite in="offsetColor" in2="offsetBlur" operator="in" result="offsetBlur"></feComposite>
          <feBlend in="SourceGraphic" in2="offsetBlur"></feBlend>
        </filter>
      </defs>
      <g v-if="isDrawDone">
        <g transform="translate(0.5,0.5)" style="visibility: visible;">
          <path v-for="(line, index) in lines" :key="index" :d="line" fill="none"
                stroke="#000000" stroke-miterlimit="10" pointer-events="all"></path>
        </g>
        <g transform="translate(0.5,0.5)" style="visibility: visible;" v-for="(item, index) in drawData" :key="index">
          <g>
            <rect :x="item.position.x" :y="item.position.y" :width="elementWidth" :height="elementHeight" rx="15"
                  ry="15" :fill="item.fillColor" :stroke="item.strokeColor"
                  pointer-events="all"></rect>
            <g>
              <foreignObject width="20" height="20" style="overflow: visible; text-align: left; position: relative;">
                <div
                  :style="`position: absolute;width: ${elementWidth}px;height: ${elementHeight}px;top: ${item.position.y}px;left: ${item.position.x}px;`">
                  <div class="element-container">
                    <div class="element-title" :style="`color: ${item.strokeColor}`" :title="item.name">
                      {{ item.name }}
                    </div>
                    <div class="add-icon" v-if="!item.isJoin">
                      <img src="/images/icon/dashboard-add-icon.svg" alt="add"
                           @click.prevent="findCompany(item, 'SUPPLIER')"/>
                    </div>
                    <div class="remove-icon" v-if="item.level === 1 && item.childIdList.length === 0 || item.level === getMaxLevel() || item.childIdList.length === 0">
                      <img src="/images/icon/dashboard-minus-icon.svg" alt="remove"
                           @click.prevent="deleteCompanyNode(item)"/>
                    </div>
                    <div class="assigned-action" v-if="item.isJoin">
                      <span @click="openAssignedTooling(item)">Assigned tooling</span>
                    </div>
                  </div>
                </div>
              </foreignObject>
            </g>
          </g>
        </g>
      </g>
    </svg>
    <div v-else class="add-root-node">
      <div class="add-icon">
        <img src="/images/icon/dashboard-add-icon.svg" alt="add"
             @click.prevent="findCompany(null, 'SUPPLIER')"/>
      </div>
    </div>
    <company-search-hierarchy :resources="resources"></company-search-hierarchy>
    <assign-tooling :resources="resources" :hierarchy-data="hierarchyData"></assign-tooling>
  </div>
</template>

<script>
  module.exports = {
    name: "user-hierarchy",
    components: {
      'company-search-hierarchy': httpVueLoader('/components/access-group/company-search-hierarchy.vue'),
      'assign-tooling': httpVueLoader('/components/access-group/assign-tooling.vue'),
    },
    props: {
      resources: Object
    },
    data() {
      return {
        currentCompanySelected: null,
        elementWidth: 140,
        elementHeight: 30,
        offsetX: 28,
        offsetY: 28,
        originalDifference: 40,
        iconSize: 25,
        differenceY: 120,
        lines: [],
        svgWidth: 0,
        svgHeight: 0,
        companyLevels: [
          {
            enabled: true,
            name: '1st Level',
            border: '1px solid black',
            fillColor: '#FFF',
            strokeColor: '#000'
          },
          {
            enabled: false,
            name: '2nd Level',
            fillColor: '#dc7f5f',
            strokeColor: '#FFF'
          },
          {
            enabled: false,
            name: '3rd Level',
            fillColor: '#3233fe',
            strokeColor: '#FFF'
          },
          {
            enabled: false,
            name: '4th Level',
            fillColor: '#2e6869',
            strokeColor: '#FFF'
          }
        ],
        drawData: [],
        hierarchyData: [],
        root: null,
        isDrawDone: false
      }
    },
    mounted() {
      let container = document.getElementById('hierarchy-container');
      this.svgWidth = container.getBoundingClientRect().width;
      this.svgHeight = container.getBoundingClientRect().height;
      this.loadData();
    },
    methods: {
      openAssignedTooling(data) {
        var child = Common.vue.getChild(this.$children, 'assign-tooling');
        if (child != null) {
          child.openAssignedToolingModal(data);
        }
      },
      resetData() {
        let legend = document.getElementById('legend');
        legend.style.width = 0 + 'px';
        this.drawData = []
        //   {
        //     id: -1,
        //     name: 'Dyson',
        //     level: 1,
        //     fillColor: '#FFF',
        //     strokeColor: '#000',
        //     parentId: 0,
        //     index: 0,
        //     companyId: null,
        //     position: {}
        //   }
        // ];
        this.lines = [];
        this.companyLevels.forEach((item, index) => {
          if (index > 0) {
            item.enabled = false;
          }
        });
      },
      loadData() {
        this.offsetX = 0;
        this.isDrawDone = false;
        axios.get('/api/access-hierarchy').then((response => {
          if (response.data && response.data.content) {
            this.hierarchyData = response.data.content;
            this.lines = [];
            if (response.data.content.length === 0) {
              return this.resetData()
            }
            this.initDrawData(response.data.content);
            // set level
            this.setLevel(this.drawData[0]);

            // set index and joinIndex of item
            console.log('this.drawData', this.drawData);
          }
          this.setIndexAndJoinIndex();
          this.draw();
          this.alignHierarchyCoordinateAndContainerDimension();
        }));
      },
      initDrawData(data) {
        let rootId = 0;
        data.sort((first, second) => {
          return first.level >= second.level ? 1: -1;
        }).forEach(item => {
          if (item.parentIdList) {
            if (item.level === 0) { // root
              rootId = item.id;
              this.drawData = [
                {
                  id: rootId,
                  name: item.company.name,
                  level: 1,
                  fillColor: '#FFF',
                  strokeColor: '#000',
                  parentId: 0,
                  childIdList: item.childIdList,
                  index: 0,
                  companyId: item.company.id,
                  position: {}
                }
              ];
            } else if (item.level === 1) { // children of root
              let level2Index = this.drawData.filter(item => item.parentId === rootId).length;
              this.drawData.push({
                id: item.id,
                name: item.company.name,
                companyId: item.company.id,
                parentId: rootId,
                childIdList: item.childIdList,
                index: level2Index,
                position: {}
              });
            } else if (item.parentIdList.length === 1) {
              let tmp = {
                id: item.id,
                name: item.company.name,
                companyId: item.company.id,
                position: {}
              };
              // index, parentId
              let parent = this.drawData.filter(element => element.companyId === item.parentIdList[0])[0];
              tmp.parentId = parent.id;
              tmp.childIdList = item.childIdList;
              this.drawData.push(tmp);
            } else {  // join element
              console.log('else')
              let tmp = {
                id: item.id,
                name: item.company.name,
                companyId: item.company.id,
                position: {},
                isJoin: true,
                accessCompanyParentRelations: item.accessCompanyParentRelations
              };
              tmp.childIdList = item.childIdList;
              tmp.parentId = this.drawData.filter(element => item.parentIdList.includes(element.companyId)).map(element => element.id).sort();
              this.drawData.push(tmp);
            }
          }
        });
      },
      setIndexAndJoinIndex() {
        // sort data by level
        this.drawData.sort((first, second) =>
        {
          if(first.level > second.level) {
            return 1;
          } else if (first.level === second.level && first.id > second.id) {
            return 1;
          }
          return -1;
        });
        this.drawData.forEach(element => {
          // only set for children from level 2
          // note: ensure that max of parent is 2
          // only set for first parent, not set from second parent and more
          if (element.level > 1) {
            let listChildrenOneParent = this.drawData.filter(item => item.parentId === element.id);
            listChildrenOneParent.forEach((item, index) => {
              item.index = index;
            });
            // only apply for first parent
            let listChildrenMultiParent = this.drawData.filter(item => item.isJoin && item.parentId[0] === element.id)
                    .sort((first, second) => {
                      if (first.parentId[1] === second.parentId[1]) {
                        return first.id > second.id ? 1 : -1;
                      }
                      return first.parentId[1] > second.parentId[1] ? 1 : -1;
                    });
            let maxIndex = 0;
            listChildrenMultiParent.forEach((item, index) => {
              if (index === 0) {
                item.index = listChildrenOneParent.length;
                item.joinIndex = 0;
                maxIndex = item.index;
              } else {
                // find element that have max index
                let maxIndexElements = listChildrenMultiParent.filter(element => element.index === maxIndex);
                let maxIndexElement = maxIndexElements[maxIndexElements.length - 1];

                let isSameJoin = true;
                item.parentId.forEach((parentId, index) => {
                  if (parentId !== maxIndexElement.parentId[index]) {
                    isSameJoin = false;
                  }
                });
                if (isSameJoin) {
                  item.index = maxIndexElement.index;
                  item.joinIndex = maxIndexElement.joinIndex + 1;
                } else {
                  item.index = maxIndexElement.index + 1;
                  item.joinIndex = 0;
                }
              }
            });
          }
        });
      },
      alignHierarchyCoordinateAndContainerDimension() {
        setTimeout(() => {
          let container = document.getElementById('hierarchy-content');
          let legend = document.getElementById('legend');
          let maxPosition = this.getMaxPosition();
          let width = this.svgWidth;
          let height = this.svgHeight;
          if (height < maxPosition.y + 2*this.offsetY) {
            height = maxPosition.y + 2*this.offsetY;
          }
          let offsetX = 28;
          if (width < maxPosition.x + 2 * offsetX) {
            width = maxPosition.x + 2 * offsetX;
          }
          // console.log('baseWidth', this.svgWidth);
          // console.log('baseHeight', this.svgHeight);
          // console.log('maxPosition', maxPosition);
          // console.log('width', width);
          // console.log('height', height);

          if (this.root.position.x + this.elementWidth/2 < width/2) {
            this.offsetX = width/2 - this.root.position.x - this.elementWidth/2;
            // this.lines = [];
            // this.draw();
          }

          let containerWidth = width;
          if (containerWidth > $('#hierarchy-container').width()) {
            containerWidth +=120;
          }
          container.setAttribute('height', height + 'px');
          container.setAttribute('width', containerWidth + 'px');
          container.style.width = containerWidth + 'px';
          legend.style.width = containerWidth + 'px';
          container.style.height = height + 'px';
          this.isDrawDone = true;
        }, 100);
      },
      getMaxPosition() {
        let maxX = 0;
        let maxY = 0;
        this.drawData.forEach(element => {
          if (element.position.x > maxX) {
            maxX = element.position.x;
          }
          if (element.position.y > maxY) {
            maxY = element.position.y;
          }
        });
        return {
          x: maxX,
          y: maxY
        }
      },
      createRelationalCompany: function (company, searchType) {
        let companyParentId = null;
        if (this.currentCompanySelected) {
          companyParentId = this.currentCompanySelected.companyId;
        }
        let payload = {
          companyId: company.id,
          companyParentId: companyParentId
        };
        axios.post('/api/access-hierarchy', payload).then(() => {
          this.loadData();
        }).catch(error => {
          Common.alert(error.response.data);
        });
      },
      deleteCompanyNode: function (node) {
        if (node.companyId) {
          axios.delete(`/api/access-hierarchy/${node.companyId}`).then(() => {
            this.loadData();
          });
        }
      },
      findCompany: function (company, searchType) {
        this.currentCompanySelected = company;
        var child = Common.vue.getChild(this.$children, 'company-search-hierarchy');
        if (child != null) {
          if (this.currentCompanySelected) {
            child.findCompany(searchType, this.currentCompanySelected.companyId);
          } else {
            child.findCompany(searchType);
          }
        }
      },
      // DRAW
      setLevel(parent) {
        let children = this.drawData.filter(item => item.parentId === parent.id || (item.parentId.length && item.parentId.includes(parent.id)));
        if (children.length > 0) {
          children.forEach(item => {
            item.level = parent.level + 1;
            this.setLevel(item);
          });
        }
      },
      setFillAndStrokeColor() {
        this.companyLevels.forEach(item => {
          item.enabled = false;
        });
        this.drawData.forEach(item => {
          let indexOfColor = item.level <= 4 ? item.level : 4;
          item.fillColor = this.companyLevels[indexOfColor - 1].fillColor;
          item.strokeColor = this.companyLevels[indexOfColor - 1].strokeColor;
          if (!this.companyLevels[indexOfColor - 1].enabled) {
            this.companyLevels[indexOfColor - 1].enabled = true;
          }
        });
      },
      setCountGroup() {
        this.drawData.forEach((element) => {
          element.countOfGroup = this.drawData.filter(item => item.parentId === element.id || (item.parentId.length > 0 && item.parentId[0] === element.id && item.joinIndex === 0)).length;
        });
      },
      getMaxLevel() {
        let maxLevel = 1;
        this.drawData.forEach(item => {
          if (item.level > maxLevel) {
            maxLevel = item.level;
          }
        });
        return maxLevel;
      },
      getMaxCountGroupOfParent(level) {
        let maxCountGroup = 1;
        this.drawData.filter(item => item.level === level).forEach(item => {
          if (item.countOfGroup > maxCountGroup) {
            maxCountGroup = item.countOfGroup;
          }
        });
        return maxCountGroup;
      },
      getParent(element) {
        let parentId;
        if (element.isJoin) {
          parentId = element.parentId[0];
        } else {
          parentId = element.parentId;
        }
        return this.drawData.filter(item => item.id === parentId)[0];
      },
      getListElementByLevel(level) {
        return this.drawData.filter(item => item.level === level);
      },
      getListChildren(parent) {
        return this.drawData.filter(item => item.parentId === parent.id || (item.parentId.length > 0 && item.parentId.includes(parent.id)));
      },
      hasOnlyOneChildren(parent) {
        let children = this.getListChildren(parent);
        children = children.filter(item => item.parentId === parent.id || item.parentId[0] === parent.id);
        return children.length === 1;
      },
      getPositionForLineInChildren(element) {
        return {
          x: element.position.x + this.elementWidth / 2,
          y: element.position.y + this.elementHeight
        }
      },
      findAboveElement(element) {
        return this.drawData.filter(item => item.isJoin && item.parentId[0] === element.parentId[0] && item.parentId[item.parentId.length - 1] === element.parentId[element.parentId.length - 1] && item.joinIndex === element.joinIndex - 1)[0];
      },
      getPositionForLineInParent(parent) {
        let positionY = parent.position.y + this.elementHeight;
        if(!parent.isJoin) {
          positionY += this.iconSize;
        }
        return {
          x: parent.position.x + this.elementWidth / 2,
          y: positionY
        }
      },
      lineToParentToChildren(parent) {
        let listChildren = this.getListChildren(parent);
        if (!listChildren.length) {
          return;
        }
        let listChildrenInSameGroup = listChildren.filter(item => !item.isJoin || item.parentId[0] === parent.id).sort((first, second) => {
          return first.index > second.index ? 1: -1;
        });
        let firstElement = listChildrenInSameGroup[0];
        if (!firstElement) {
          firstElement = listChildren[0];
        }
        let parentPosition = this.getPositionForLineInParent(parent);
        let firstPosition = this.getPositionForLineInChildren(firstElement);
        let middlePositionY = firstPosition.y - this.differenceY / 2;
        if (parent.countOfGroup > 1) {
          // detect first children
          // first children will be overwritten when exist element that have second parent is this element
          let joinChildrenWithSecondParents = listChildren.filter(children => children.isJoin && children.joinIndex === 0 && children.parentId[1] === parent.id);
          if (joinChildrenWithSecondParents.length > 0) {
            firstElement = joinChildrenWithSecondParents.sort((first, second) => {
              return first.parentId[0] >= second.parentId[0] ? 1: -1;
            })[0];
            firstPosition = this.getPositionForLineInChildren(firstElement);
          }

          // detect first element and last element
          let lastElement = listChildren.filter(item => item.index === parent.countOfGroup - 1 && (!item.isJoin || (item.joinIndex === 0 && item.parentId[0] === parent.id)))[0];
          if(!lastElement){
            lastElement = listChildren[listChildren.length - 1];
          }
          // checking with join
          let lastPosition = this.getPositionForLineInChildren(lastElement);
          let otherPositions = [];
          listChildren.filter(item => item.id !== firstElement.id && item.id !== lastElement.id)
            .filter(item => !item.isJoin || item.joinIndex === 0).forEach(item => {
            otherPositions.push(this.getPositionForLineInChildren(item));
          });
          // line first and last
          let lineParams = [];
          lineParams.push(`M ${firstPosition.x} ${firstPosition.y} L ${firstPosition.x} ${middlePositionY + 10} Q ${firstPosition.x} ${middlePositionY} ${firstPosition.x + 10} ${middlePositionY}`);
          lineParams.push(`L ${lastPosition.x - 10} ${middlePositionY} Q ${lastPosition.x} ${middlePositionY} ${lastPosition.x} ${middlePositionY + 10} L ${lastPosition.x} ${lastPosition.y}`);
          this.lines.push(lineParams.join(' '));
          // line other position of children
          otherPositions.forEach(position => {
            this.lines.push(`M ${position.x} ${position.y} L ${position.x} ${middlePositionY}`);
          });
          // below join children
          listChildren.filter(item => item.joinIndex > 0).forEach(item => {
            this.lines.push(`M ${item.position.x} ${item.position.y + this.elementHeight / 2} L ${item.position.x - this.originalDifference / 2 + 10} ${item.position.y + this.elementHeight / 2} Q ${item.position.x - this.originalDifference / 2} ${item.position.y + this.elementHeight / 2} ${item.position.x - this.originalDifference / 2} ${item.position.y + this.elementHeight / 2 - 10} L ${item.position.x - this.originalDifference / 2} ${middlePositionY}`);
            this.lines.push(`M ${item.position.x + this.elementWidth} ${item.position.y + this.elementHeight / 2} L ${item.position.x + this.elementWidth + this.originalDifference / 2 - 10} ${item.position.y + this.elementHeight / 2} Q ${item.position.x + this.elementWidth + this.originalDifference / 2} ${item.position.y + this.elementHeight / 2} ${item.position.x + this.elementWidth + this.originalDifference / 2} ${item.position.y + this.elementHeight / 2 - 10} L ${item.position.x + this.elementWidth + this.originalDifference / 2} ${middlePositionY}`);
          });

        } else if (parent.countOfGroup === 1) {
          if (listChildren.length > 1) {
            listChildren.sort((first, second) => {
              if (first.parentId.length && !second.parentId.length) {
                return -1;
              } else if (!first.parentId.length && second.parentId.length) {
                return 1;
              } else if (first.parentId.length && second.parentId.length) {
                if (first.parentId[0] === second.parentId[0]) {
                  return first.id >= second.id ? 1 : -1;
                } else {
                  return first.parentId[0] > second.parentId[0] ? 1: -1;
                }
              }
              return first.id >= second.id ? 1 : -1
            });
            let firstElement = listChildren[0];
            let lastElement = listChildren[listChildren.length - 1];
            firstPosition = this.getPositionForLineInChildren(firstElement);
            let lastPosition = this.getPositionForLineInChildren(lastElement);
            let otherPositions = [];
            listChildren.filter(item => item.id !== firstElement.id && item.id !== lastElement.id)
              .filter(item => !item.isJoin || item.joinIndex === 0).forEach(item => {
              otherPositions.push(this.getPositionForLineInChildren(item));
            });

            this.lines.push(`M ${firstPosition.x} ${firstPosition.y} L ${firstPosition.x} ${middlePositionY + 10} Q ${firstPosition.x} ${middlePositionY} ${firstPosition.x + 10} ${middlePositionY} L ${lastPosition.x} ${middlePositionY}`);
            otherPositions.forEach(position => {
              this.lines.push(`M ${position.x} ${position.y} L ${position.x} ${middlePositionY}`);
            });
            this.lines.push(`M ${lastPosition.x} ${lastPosition.y} L ${lastPosition.x} ${middlePositionY}`)
          } else {
            this.lines.push(`M ${firstPosition.x} ${firstPosition.y} L ${firstPosition.x} ${middlePositionY}`)
          }

        } else {
          if (listChildren.length > 0) {
            this.lines.push(`M ${firstPosition.x} ${firstPosition.y} L ${firstPosition.x} ${middlePositionY + 10} Q ${firstPosition.x} ${middlePositionY} ${firstPosition.x + 10} ${middlePositionY} L ${parentPosition.x} ${middlePositionY}  L ${parentPosition.x} ${parentPosition.y}`);
          }
        }
        // line to parent
        this.lines.push(`M ${parentPosition.x} ${parentPosition.y} ${parentPosition.x} ${middlePositionY}`);
      },
      draw() {
        let firstXPositionByLevel = {};
        let differenceByLevel = {};

        this.setFillAndStrokeColor();
        this.setCountGroup();
        // set difference of max level element
        let maxLevel = this.getMaxLevel();
        this.drawData.filter(element => element.level === maxLevel).forEach(item => {
          item.differenceLeft = 0;
          let parent = this.getParent(item);
          if (parent) {
            if (item.index < parent.countOfGroup - 1) {
              item.differenceRight = this.originalDifference;
            }else {
              item.differenceRight = 0;
            }
          }

        });

        for (let level = maxLevel - 1; level > 0; level--) {
          let elementInLevels = this.drawData.filter(element => element.level === level);
          elementInLevels.forEach(parent => {
            // find children at layer 1
            let superParent = this.getParent(parent);
            let children = this.drawData.filter(element => element.parentId === parent.id || element.isJoin && element.joinIndex === 0 && element.parentId.includes(parent.id));
            if (children.length > 0) {
              // checking total width
              let totalWidth = 0;
              children.forEach(item => {
                totalWidth += this.elementWidth + item.differenceLeft + item.differenceRight;
              });

              let joinedChildrens = this.drawData.filter(element => element.isJoin && element.joinIndex === 0 && element.parentId.includes(parent.id));
              if (parent.index === 0){
                if (parent.countOfGroup > 1) {
                  parent.differenceLeft = totalWidth / 2;
                } else {
                  parent.differenceLeft = 0;
                }

                // checking if join with next sibling
                if (joinedChildrens.length === 0) {
                  parent.differenceRight = totalWidth / 2 + this.originalDifference;
                } else if (joinedChildrens.length === 1) {
                  let otherParentId = joinedChildrens[0].parentId[1];
                  let otherParent = this.drawData.filter(element => element.id === otherParentId)[0];
                  if (otherParent.index === 1) { // if is next sibling
                    parent.differenceRight = totalWidth / 2;
                  } else {
                    parent.differenceRight = totalWidth / 2 + this.originalDifference;
                  }
                } else {
                  parent.differenceRight = totalWidth / 2 + this.originalDifference;
                }
              } else {
                if (joinedChildrens.length === 0) {
                  if (parent.countOfGroup > 1) {
                    parent.differenceLeft = totalWidth / 2;
                  } else {
                    parent.differenceLeft = 0;
                  }

                  parent.differenceRight = totalWidth / 2 + this.originalDifference;
                } else if (joinedChildrens.length === 1) {
                  // other parent
                  let otherParentId = joinedChildrens[0].parentId.filter(parentId => parentId !== parent.id)[0];
                  let otherParent = this.drawData.filter(element => element.id === otherParentId)[0];
                  if (otherParent.index === parent.index - 1) { // join in left
                    parent.differenceLeft = totalWidth/2 - this.elementWidth;
                    parent.differenceRight = totalWidth/2 + this.originalDifference;
                  } else if (otherParent.index === parent.index + 1) { // join in right
                    parent.differenceLeft = totalWidth/2;
                    parent.differenceRight = totalWidth/2;
                  } else {
                    parent.differenceLeft = totalWidth/2;
                    parent.differenceRight = totalWidth/2 + this.originalDifference;
                  }
                } else {  // count of join element >= 2
                  // default case
                  parent.differenceLeft = totalWidth/2;
                  parent.differenceRight = totalWidth/2 + this.originalDifference;
                  joinedChildrens.forEach(joinedChildren => {
                    let otherParentId = joinedChildren.parentId.filter(parentId => parentId !== parent.id)[0];
                    let otherParent = this.drawData.filter(element => element.id === otherParentId)[0];
                    if (otherParent.index === parent.index - 1) {
                      // checking with last element of previous sibling has parent is current parent
                      let childrenOfOtherParentInFirstLayers = this.drawData.filter(element => element.isJoin && element.joinIndex === 0 && element.parentId.includes(otherParentId));
                      if (childrenOfOtherParentInFirstLayers.length > 0) {
                        let lastChildren = childrenOfOtherParentInFirstLayers[childrenOfOtherParentInFirstLayers.length - 1];
                        if (lastChildren.parentId.includes(parent.id)) {
                          parent.differenceLeft = totalWidth/2 - this.elementWidth;
                        }
                      }

                    } else if (otherParent.index === parent.index + 1) { // if join with next sibling
                      if (joinedChildren.index === parent.countOfGroup - 1) { // and join element is last element of current parent
                        parent.differenceRight = totalWidth/2;
                      }
                    }
                  });
                }
              }
              if (superParent && parent.index === superParent.countOfGroup - 1) {  // last element
                parent.differenceRight = totalWidth/2;
                if (parent.countOfGroup === 1) {
                  parent.differenceRight = 0;
                }
              }
            } else {
              parent.differenceLeft = 0;
              parent.differenceRight = this.originalDifference;
              if (superParent && parent.index === superParent.countOfGroup - 1) {  // last element
                parent.differenceRight = 0;
              }
            }
          });
        }

        // set position for element that has index = 0
        this.drawData.forEach((element, index) => {
          element.position = {
            x: 0,
            y: 0
          };
          if (element.level === 1) {

            element.position.x = element.differenceLeft;
            element.position.y = this.offsetY;
            this.root = element;
          } else {
            let parent = this.getParent(element);
            if (element.index === 0) {  // first element

              if (this.hasOnlyOneChildren(parent)) {
                element.position.x = parent.position.x;
              } else {
                element.position.x = parent.position.x - parent.differenceLeft + element.differenceLeft + this.elementWidth/2;
              }
              // if (parent.countOfGroup > 1) {
              //   element.position.x += this.elementWidth/2;
              // }
              element.position.y = this.offsetY + (element.level - 1) * this.differenceY;
            } else {
              // find previous sibling
              let previousSibling = this.drawData.filter(item => item.index === element.index - 1 && (item.parentId === parent.id || (item.isJoin && item.joinIndex === 0 && item.parentId[0] === parent.id)))[0];
              if (previousSibling) {
                if (previousSibling.position.x !== undefined) {
                  element.position.x = previousSibling.position.x + previousSibling.differenceRight + element.differenceLeft + this.elementWidth;
                  element.position.y = this.offsetY + (element.level - 1) * this.differenceY;
                  if (element.isJoin && element.joinIndex > 0) {
                    element.position.y = this.offsetY + (element.level - 1) * this.differenceY + element.joinIndex * this.differenceY;
                  }
                } else {
                  // calculate difference of other sibling
                  let listFirstLayerChildren = this.getListChildren(parent).filter(item => !item.isJoin || (item.parentId[0] === parent.id && item.joinIndex === 0))
                    .sort((first, second) => {
                      return first.index > second.index ? 1 : -1;
                    });

                  if (listFirstLayerChildren.length > 0) {
                    let firstSibling = listFirstLayerChildren[0];
                    firstSibling.position.x = parent.position.x - parent.differenceLeft + firstSibling.differenceLeft;
                    firstSibling.position.y = this.offsetY + (firstSibling.level - 1) * this.differenceY;
                    element.position.y = firstSibling.position.y;
                    previousSibling = firstSibling;
                    for (let i = 1; i < listFirstLayerChildren.length; i++) {
                      let currentElement = listFirstLayerChildren[i];
                      currentElement.position.x = previousSibling.position.x + previousSibling.differenceRight + currentElement.differenceLeft + this.elementWidth;
                      currentElement.position.y = this.offsetY + (currentElement.level - 1) * this.differenceY;
                      if (currentElement.isJoin && currentElement.joinIndex > 0) {
                        currentElement.position.y = this.offsetY + (currentElement.level - 1) * this.differenceY + currentElement.joinIndex * this.differenceY;
                      }
                      previousSibling = currentElement;
                    }
                  }
                }

              }
            }
          }
        });
        // justify for root
        let rootChildrens = this.drawData.filter(element => element.parentId === this.root.id);
        if (rootChildrens.length > 0) {
          this.root.position.x = (rootChildrens[0].position.x + rootChildrens[rootChildrens.length - 1].position.x)/2;
        }

        // draw line to children
        this.drawData.forEach(item => {
          if (item.level < maxLevel) {
            this.lineToParentToChildren(item);
          }
        });
      }
      // re draw if not justify for all
      // END DRAW
    }
  }
</script>

<style scoped>
</style>