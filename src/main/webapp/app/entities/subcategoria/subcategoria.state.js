(function() {
    'use strict';

    angular
        .module('ecofruitApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('subcategoria', {
            parent: 'entity',
            url: '/subcategoria?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ecofruitApp.subcategoria.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/subcategoria/subcategorias.html',
                    controller: 'SubcategoriaController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('subcategoria');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('subcategoria-detail', {
            parent: 'entity',
            url: '/subcategoria/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ecofruitApp.subcategoria.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/subcategoria/subcategoria-detail.html',
                    controller: 'SubcategoriaDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('subcategoria');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Subcategoria', function($stateParams, Subcategoria) {
                    return Subcategoria.get({id : $stateParams.id}).$promise;
                }]
            }
        })
        .state('subcategoria.new', {
            parent: 'subcategoria',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/subcategoria/subcategoria-dialog.html',
                    controller: 'SubcategoriaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                nombre: null,
                                descripcion: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('subcategoria', null, { reload: true });
                }, function() {
                    $state.go('subcategoria');
                });
            }]
        })
        .state('subcategoria.edit', {
            parent: 'subcategoria',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/subcategoria/subcategoria-dialog.html',
                    controller: 'SubcategoriaDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Subcategoria', function(Subcategoria) {
                            return Subcategoria.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('subcategoria', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('subcategoria.delete', {
            parent: 'subcategoria',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/subcategoria/subcategoria-delete-dialog.html',
                    controller: 'SubcategoriaDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Subcategoria', function(Subcategoria) {
                            return Subcategoria.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('subcategoria', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
