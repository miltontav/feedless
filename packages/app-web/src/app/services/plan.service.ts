import { inject, Injectable } from '@angular/core';
import {
  CancelPlan,
  FieldWrapper,
  GqlCancelPlanMutation,
  GqlCancelPlanMutationVariables,
  GqlCursor,
  GqlPlansQuery,
  GqlPlansQueryVariables,
  Plans,
  Scalars,
} from '../../generated/graphql';
import { ApolloClient, FetchPolicy } from '@apollo/client/core';
import { ArrayElement } from '../types';

export type Plan = ArrayElement<GqlPlansQuery['plans']>;

@Injectable({
  providedIn: 'root',
})
export class PlanService {
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);

  constructor() {}

  async fetchPlans(
    cursor: GqlCursor,
    fetchPolicy: FetchPolicy = 'cache-first',
  ): Promise<Plan[]> {
    return this.apollo
      .query<GqlPlansQuery, GqlPlansQueryVariables>({
        query: Plans,
        variables: {
          cursor,
        },
        fetchPolicy,
      })
      .then((response) => response.data.plans);
  }

  async cancelPlanSubscription(id: string) {
    await this.apollo.mutate<
      GqlCancelPlanMutation,
      GqlCancelPlanMutationVariables
    >({
      mutation: CancelPlan,
      variables: {
        planId: id,
      },
    });
  }
}
