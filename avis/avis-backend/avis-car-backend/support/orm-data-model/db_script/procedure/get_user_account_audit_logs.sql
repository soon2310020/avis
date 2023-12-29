drop procedure if exists get_user_account_audit_logs;

create procedure get_user_account_audit_logs(user_code varchar(255), start_date date, end_date date)
begin
    select rev.rev                                                            as id,
           if(u.revtype = 0, 'CREATE', if(u.is_deleted, 'DELETE', 'UPDATE'))  as action,
           u.code                                                             as code,
           u.name                                                             as name,
           ur.name                                                            as role,
           b.name                                                             as branch,
           d.name                                                             as department,
           if(u.active, 'Active', 'Inactive')                                 as status,
           cre.code                                                           as creator_code,
           cre.name                                                           as creator_name,
           convert_tz(rev.created_at, '+00:00', '+07:00')                     as datetime
    from revinfo rev
             left join action_type at on rev.action_type = at.id
             left join user cre on rev.created_by = cre.id
             left join user_aud u on rev.rev = u.rev
             left join user_role ur on u.user_role_id = ur.id
             left join branch b on u.branch_id = b.id
             left join department d on u.department_id = d.id
             left join user uo on u.unit_operator_id = uo.id
    where rev.created_by is not null
      and at.code = 'USER'
      and u.user_role_id in (1, 2)
      and (user_code is null or u.code like upper(user_code))
      and (start_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) >= start_date)
      and (end_date is null or date(convert_tz(rev.created_at, '+00:00', '+07:00')) <= end_date)
    order by rev.rev, u.code;
end;

call get_user_account_audit_logs(null, null, null);